package org.folio.linked.data.service.resource;

import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.linked.data.util.ResourceUtils.extractWorkFromInstance;
import static org.folio.linked.data.util.ResourceUtils.getPrimaryMainTitles;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.domain.dto.HubField;
import org.folio.linked.data.domain.dto.InstanceField;
import org.folio.linked.data.domain.dto.ResourceIdDto;
import org.folio.linked.data.domain.dto.ResourceRequestDto;
import org.folio.linked.data.domain.dto.ResourceResponseDto;
import org.folio.linked.data.domain.dto.ResourceSubgraphViewDto;
import org.folio.linked.data.domain.dto.SearchResourcesRequestDto;
import org.folio.linked.data.domain.dto.WorkField;
import org.folio.linked.data.exception.RequestProcessingExceptionBuilder;
import org.folio.linked.data.mapper.dto.ResourceSubgraphViewDtoMapper;
import org.folio.linked.data.mapper.dto.resource.ResourceDtoMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceSubgraphView;
import org.folio.linked.data.repo.FolioMetadataRepository;
import org.folio.linked.data.repo.ResourceRepository;
import org.folio.linked.data.repo.ResourceSubgraphViewRepository;
import org.folio.linked.data.service.profile.ResourceProfileLinkingService;
import org.folio.linked.data.service.resource.copy.ResourceCopyService;
import org.folio.linked.data.service.resource.events.ResourceEventsPublisher;
import org.folio.linked.data.service.resource.graph.ResourceGraphService;
import org.folio.linked.data.service.resource.marc.RawMarcService;
import org.folio.linked.data.service.resource.meta.MetadataService;
import org.folio.linked.data.util.ResourceUtils;
import org.folio.spring.FolioExecutionContext;
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
  private final ResourceEventsPublisher eventsPublisher;
  private final FolioExecutionContext folioExecutionContext;
  private final ResourceCopyService resourceCopyService;
  private final RawMarcService rawMarcService;
  private final ResourceProfileLinkingService resourceProfileService;
  private final ResourceSubgraphViewRepository resourceSubgraphViewRepository;
  private final ResourceSubgraphViewDtoMapper resourceSubgraphViewDtoMapper;

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
  @Transactional(readOnly = true)
  public Set<ResourceSubgraphViewDto> searchResources(SearchResourcesRequestDto request) {
    return resourceSubgraphViewRepository.findByInventoryIdIn(request.getInventoryIds())
      .stream()
      .map(ResourceSubgraphView::getResourceSubgraph)
      .flatMap(resourceJson -> resourceSubgraphViewDtoMapper.fromJson(resourceJson).stream())
      .collect(Collectors.toSet());
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
      eventsPublisher.emitEventForDelete(resource);
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
    var saveResult = resourceGraphService.saveMergingGraph(resourceToSave);
    resourceProfileService.linkResourceToProfile(saveResult.rootResource(), profileId);
    eventsPublisher.emitEventsForCreate(saveResult);
    return saveResult.rootResource();
  }

  private Resource updateResourceAndPublishEvents(Resource resourceToSave, Resource old, Integer profileId) {
    resourceCopyService.copyEdgesAndProperties(old, resourceToSave);
    metadataService.ensure(resourceToSave, old.getFolioMetadata());
    resourceToSave.setCreatedDate(old.getCreatedDate())
      .setVersion(old.getVersion() + 1)
      .setCreatedBy(old.getCreatedBy())
      .setUpdatedBy(folioExecutionContext.getUserId());
    var unmappedMarc = rawMarcService.getRawMarc(old).orElse(null);
    var saveResult = resourceGraphService.saveMergingGraph(resourceToSave);
    var savedResource = saveResult.rootResource();
    rawMarcService.saveRawMarc(savedResource, unmappedMarc);
    resourceProfileService.linkResourceToProfile(savedResource, profileId);
    eventsPublisher.emitEventsForUpdate(old, saveResult);
    return savedResource;
  }

  private String toLogString(ResourceRequestDto requestDto) {
    return switch (requestDto.getResource()) {
      case InstanceField instance -> "Instance, Title: " + getPrimaryMainTitles(instance.getInstance().getTitle());
      case WorkField work -> "Work, Title: " + getPrimaryMainTitles(work.getWork().getTitle());
      case HubField hub -> "Hub, Title: " + getPrimaryMainTitles(hub.getHub().getTitle());
      default -> throw exceptionBuilder.badRequestException(
        "Unsupported DTO", requestDto.getResource().getClass().getSimpleName());
    };
  }

  private Integer getProfileId(ResourceRequestDto requestDto) {
    return switch (requestDto.getResource()) {
      case InstanceField instance -> instance.getInstance().getProfileId();
      case WorkField work -> work.getWork().getProfileId();
      case HubField hub -> hub.getHub().getProfileId();
      default -> throw exceptionBuilder.badRequestException(
        "Unsupported DTO", requestDto.getResource().getClass().getSimpleName());
    };
  }
}
