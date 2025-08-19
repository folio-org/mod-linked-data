package org.folio.linked.data.service.resource;

import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.linked.data.util.ResourceUtils.extractWorkFromInstance;
import static org.folio.linked.data.util.ResourceUtils.getPrimaryMainTitles;

import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.domain.dto.InstanceField;
import org.folio.linked.data.domain.dto.ResourceIdDto;
import org.folio.linked.data.domain.dto.ResourceRequestDto;
import org.folio.linked.data.domain.dto.ResourceResponseDto;
import org.folio.linked.data.domain.dto.WorkField;
import org.folio.linked.data.exception.RequestProcessingExceptionBuilder;
import org.folio.linked.data.mapper.dto.resource.ResourceDtoMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.event.ResourceCreatedEvent;
import org.folio.linked.data.model.entity.event.ResourceDeletedEvent;
import org.folio.linked.data.model.entity.event.ResourceReplacedEvent;
import org.folio.linked.data.model.entity.event.ResourceUpdatedEvent;
import org.folio.linked.data.repo.FolioMetadataRepository;
import org.folio.linked.data.repo.ResourceRepository;
import org.folio.linked.data.service.profile.ResourceProfileLinkingService;
import org.folio.linked.data.service.resource.copy.ResourceCopyService;
import org.folio.linked.data.service.resource.graph.ResourceGraphService;
import org.folio.linked.data.service.resource.marc.RawMarcService;
import org.folio.linked.data.service.resource.meta.MetadataService;
import org.folio.linked.data.util.ResourceUtils;
import org.folio.spring.FolioExecutionContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
@Transactional
@RequiredArgsConstructor
public class ResourceServiceImpl implements ResourceService {

  private final ResourceRepository resourceRepo;
  private final MetadataService metadataService;
  private final ResourceDtoMapper resourceDtoMapper;
  private final ResourceGraphService resourceGraphService;
  private final FolioMetadataRepository folioMetadataRepo;
  private final RequestProcessingExceptionBuilder exceptionBuilder;
  private final ApplicationEventPublisher applicationEventPublisher;
  private final FolioExecutionContext folioExecutionContext;
  private final ResourceCopyService resourceCopyService;
  private final RawMarcService rawMarcService;
  private final ResourceProfileLinkingService resourceProfileService;

  @Override
  public ResourceResponseDto createResource(ResourceRequestDto resourceDto) {
    log.info("Received request to create new resource - {}", toLogString(resourceDto));
    var mapped = resourceDtoMapper.toEntity(resourceDto);
    rejectDuplication(mapped);
    log.debug("createResource\n[{}]\nfrom DTO [{}]", mapped, resourceDto);
    var persisted = createResourceAndPublishEvents(mapped, getProfileId(resourceDto));
    return resourceDtoMapper.toDto(persisted);
  }

  @Override
  @Transactional(readOnly = true)
  public ResourceResponseDto getResourceById(Long id) {
    var resource = getResource(id);
    return resourceDtoMapper.toDto(resource);
  }

  @Override
  @Transactional(readOnly = true)
  public ResourceIdDto getResourceIdByInventoryId(String inventoryId) {
    return folioMetadataRepo.findIdByInventoryId(inventoryId)
      .map(idOnly -> new ResourceIdDto().id(String.valueOf(idOnly.getId())))
      .orElseThrow(() -> exceptionBuilder.notFoundLdResourceByInventoryIdException(inventoryId));
  }

  @Override
  public ResourceResponseDto updateResource(Long id, ResourceRequestDto resourceDto) {
    log.info("Received request to update resource {} - {}", id, toLogString(resourceDto));
    log.debug("updateResource [{}] from DTO [{}]", id, resourceDto);
    var mapped = resourceDtoMapper.toEntity(resourceDto);
    rejectInstanceOfAnotherWork(id, mapped);
    var existed = getResource(id);
    var oldResource = new Resource(existed);
    resourceGraphService.breakEdgesAndDelete(existed);
    var newResource = updateResourceAndPublishEvents(mapped, oldResource, getProfileId(resourceDto));
    return resourceDtoMapper.toDto(newResource);
  }

  @Override
  public void deleteResource(Long id) {
    log.info("deleteResource [{}]", id);
    resourceRepo.findById(id).ifPresent(resource -> {
      resourceGraphService.breakEdgesAndDelete(resource);
      applicationEventPublisher.publishEvent(new ResourceDeletedEvent(resource));
    });
  }

  @Async
  @Override
  public void updateIndexDateBatch(Set<Long> ids) {
    resourceRepo.updateIndexDateBatch(ids);
  }

  private void rejectDuplication(Resource resourceToSave) {
    if (resourceRepo.existsById(resourceToSave.getId())) {
      log.error("Resource with same ID {} already exists", resourceToSave.getId());
      throw exceptionBuilder.alreadyExistsException("ID", String.valueOf(resourceToSave.getId()));
    }
  }

  private void rejectInstanceOfAnotherWork(Long requestId, Resource resourceToSave) {
    if (resourceToSave.isNotOfType(INSTANCE) || Objects.equals(requestId, resourceToSave.getId())) {
      return;
    }

    var incomingInstanceWorkId = extractWorkFromInstance(resourceToSave)
      .map(Resource::getId);
    var existedInstanceWorkId = resourceRepo.findById(resourceToSave.getId())
      .flatMap(ResourceUtils::extractWorkFromInstance)
      .map(Resource::getId);

    if (existedInstanceWorkId.isPresent() && !existedInstanceWorkId.equals(incomingInstanceWorkId)) {
      log.error("Instance {} is already connected to work {}. Connecting the instance to another work {} "
        + "is not allowed.", resourceToSave.getId(), existedInstanceWorkId, incomingInstanceWorkId);
      throw exceptionBuilder.alreadyExistsException("ID", String.valueOf(resourceToSave.getId()));
    }
  }

  private Resource getResource(Long id) {
    return resourceRepo.findById(id)
      .orElseThrow(() -> exceptionBuilder.notFoundLdResourceByIdException("Resource", String.valueOf(id)));
  }

  private Resource createResourceAndPublishEvents(Resource resourceToSave, Integer profileId) {
    metadataService.ensure(resourceToSave);
    var persisted = resourceGraphService.saveMergingGraph(resourceToSave);
    resourceProfileService.linkResourceToProfile(persisted, profileId);
    applicationEventPublisher.publishEvent(new ResourceCreatedEvent(persisted));
    return persisted;
  }

  private Resource updateResourceAndPublishEvents(Resource resourceToSave, Resource old, Integer profileId) {
    resourceCopyService.copyEdgesAndProperties(old, resourceToSave);
    metadataService.ensure(resourceToSave, old.getFolioMetadata());
    resourceToSave.setCreatedDate(old.getCreatedDate())
      .setVersion(old.getVersion() + 1)
      .setCreatedBy(old.getCreatedBy())
      .setUpdatedBy(folioExecutionContext.getUserId());
    var unmappedMarc = rawMarcService.getRawMarc(old).orElse(null);
    var saved = resourceGraphService.saveMergingGraph(resourceToSave);
    rawMarcService.saveRawMarc(saved, unmappedMarc);
    resourceProfileService.linkResourceToProfile(saved, profileId);
    if (Objects.equals(old.getId(), saved.getId())) {
      applicationEventPublisher.publishEvent(new ResourceUpdatedEvent(saved));
    } else {
      applicationEventPublisher.publishEvent(new ResourceReplacedEvent(old, saved.getId()));
    }
    return saved;
  }

  private String toLogString(ResourceRequestDto requestDto) {
    return switch (requestDto.getResource()) {
      case InstanceField instance -> "Instance, Title: " + getPrimaryMainTitles(instance.getInstance().getTitle());
      case WorkField work -> "Work, Title: " + getPrimaryMainTitles(work.getWork().getTitle());
      default -> throw exceptionBuilder.badRequestException(
        "Unsupported DTO", requestDto.getResource().getClass().getSimpleName());
    };
  }

  private Integer getProfileId(ResourceRequestDto requestDto) {
    return switch (requestDto.getResource()) {
      case InstanceField instance -> instance.getInstance().getProfileId();
      case WorkField work -> work.getWork().getProfileId();
      default -> throw exceptionBuilder.badRequestException(
        "Unsupported DTO", requestDto.getResource().getClass().getSimpleName());
    };
  }
}
