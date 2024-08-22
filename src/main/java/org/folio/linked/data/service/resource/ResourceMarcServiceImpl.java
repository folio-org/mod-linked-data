package org.folio.linked.data.service.resource;

import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.linked.data.util.BibframeUtils.extractWorkFromInstance;
import static org.folio.linked.data.util.Constants.IS_NOT_FOUND;
import static org.folio.linked.data.util.Constants.RESOURCE_WITH_GIVEN_ID;

import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.ld.dictionary.model.FolioMetadata;
import org.folio.linked.data.domain.dto.ResourceMarcViewDto;
import org.folio.linked.data.exception.NotFoundException;
import org.folio.linked.data.exception.ValidationException;
import org.folio.linked.data.mapper.ResourceModelMapper;
import org.folio.linked.data.mapper.dto.ResourceDtoMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceTypeEntity;
import org.folio.linked.data.model.entity.event.ResourceCreatedEvent;
import org.folio.linked.data.model.entity.event.ResourceEvent;
import org.folio.linked.data.model.entity.event.ResourceReplacedEvent;
import org.folio.linked.data.model.entity.event.ResourceUpdatedEvent;
import org.folio.linked.data.repo.FolioMetadataRepository;
import org.folio.linked.data.repo.ResourceEdgeRepository;
import org.folio.linked.data.repo.ResourceRepository;
import org.folio.marc4ld.service.ld2marc.Bibframe2MarcMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
@Transactional
@RequiredArgsConstructor
public class ResourceMarcServiceImpl implements ResourceMarcService {

  private final ResourceRepository resourceRepo;
  private final ResourceEdgeRepository edgeRepo;
  private final ResourceDtoMapper resourceDtoMapper;
  private final ResourceModelMapper resourceModelMapper;
  private final Bibframe2MarcMapper bibframe2MarcMapper;
  private final ResourceGraphService resourceGraphService;
  private final FolioMetadataRepository folioMetadataRepository;
  private final ApplicationEventPublisher applicationEventPublisher;


  public Long saveMarcResource(org.folio.ld.dictionary.model.Resource modelResource) {
    var incomingSrsId = ofNullable(modelResource.getFolioMetadata())
      .map(FolioMetadata::getSrsId)
      .orElse(null);
    var existedSrsId = folioMetadataRepository.findById(modelResource.getId())
      .map(org.folio.linked.data.model.entity.FolioMetadata::getSrsId)
      .orElse(null);
    var mapped = resourceModelMapper.toEntity(modelResource);

    if (resourceRepo.existsById(modelResource.getId())) {
      return updateResource(modelResource.getId(), incomingSrsId, existedSrsId, mapped);
    }
    if (nonNull(existedSrsId)) {
      return replaceResource(modelResource.getId(), incomingSrsId, existedSrsId, mapped);
    }
    return createResource(modelResource.getId(), incomingSrsId, mapped);
  }

  @Override
  @Transactional(readOnly = true)
  public ResourceMarcViewDto getResourceMarcView(Long id) {
    var resource = resourceRepo.findById(id)
      .orElseThrow(() -> new NotFoundException(RESOURCE_WITH_GIVEN_ID + id + IS_NOT_FOUND));
    validateMarkViewSupportedType(resource);
    var resourceModel = resourceModelMapper.toModel(resource);
    var marc = bibframe2MarcMapper.toMarcJson(resourceModel);
    return resourceDtoMapper.toMarcViewDto(resource, marc);
  }

  private void validateMarkViewSupportedType(Resource resource) {
    if (resource.isOfType(INSTANCE)) {
      return;
    }
    throw new ValidationException(
      "Resource is not supported for MARC view",
      "type", resource.getTypes().stream()
      .map(ResourceTypeEntity::getUri)
      .collect(Collectors.joining(", ", "[", "]"))
    );
  }

  private Long createResource(Long incomingId, String incomingSrsId, Resource mapped) {
    logMarcAction(incomingId, incomingSrsId, "not found by id and srsId", "created");
    return saveAndPublishEvent(mapped, ResourceCreatedEvent::new);
  }

  private Long replaceResource(Long incomingId, String incomingSrsId, String existedSrsId, Resource mapped) {
    return resourceRepo.findByFolioMetadataSrsId(existedSrsId)
      .map(Resource::new)
      .map(existedBySrsId -> {
        logMarcAction(incomingId, incomingSrsId,
          "not found by id, but found by srsId [" + existedSrsId + "]", "replaced");
        return saveAndPublishEvent(mapped,
          saved -> new ResourceReplacedEvent(existedBySrsId, saved));
      })
      .orElseThrow(() -> new NotFoundException("Resource not found by existed srsId: " + existedSrsId));
  }

  private Long updateResource(Long incomingId, String incomingSrsId, String existedSrsId, Resource mapped) {
    logMarcAction(incomingId, incomingSrsId,
      "found by id [" + incomingId + "] with srsId [" + existedSrsId + "]", "updated");
    return saveAndPublishEvent(mapped, ResourceUpdatedEvent::new);
  }

  private void logMarcAction(Long incomingId, String incomingSrsId, String existence, String action) {
    log.info("Incoming marc resource [id {}, srsId {}] is {} and will be {}",
      incomingId, incomingSrsId, existence, action);
  }

  private Long saveAndPublishEvent(Resource mapped, Function<Resource, ResourceEvent> resourceEventSupplier) {
    var newResource = resourceGraphService.saveMergingGraph(mapped);
    refreshWork(newResource);
    var event = resourceEventSupplier.apply(newResource);
    if (event instanceof ResourceReplacedEvent rre) {
      resourceGraphService.breakEdgesAndDelete(rre.previous());
    }
    applicationEventPublisher.publishEvent(event);
    return newResource.getId();
  }

  private void refreshWork(Resource resource) {
    if (resource.isOfType(INSTANCE)) {
      extractWorkFromInstance(resource)
        .ifPresent(work -> {
          edgeRepo.findByIdTargetHash(work.getId())
            .forEach(work::addIncomingEdge);
          addOutgoingEdges(work);
        });
    }
  }

  private void addOutgoingEdges(Resource resource) {
    edgeRepo.findByIdSourceHash(resource.getId())
      .forEach(resource::addOutgoingEdge);
  }
}
