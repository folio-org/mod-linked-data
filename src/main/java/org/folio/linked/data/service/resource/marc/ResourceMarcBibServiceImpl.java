package org.folio.linked.data.service.resource.marc;

import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static org.folio.ld.dictionary.PredicateDictionary.ADMIN_METADATA;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.ld.dictionary.model.ResourceSource.LINKED_DATA;
import static org.folio.linked.data.util.Constants.IS_NOT_FOUND;
import static org.folio.linked.data.util.Constants.MSG_NOT_FOUND_IN;
import static org.folio.linked.data.util.Constants.RESOURCE_WITH_GIVEN_ID;
import static org.folio.linked.data.util.ResourceUtils.extractWorkFromInstance;
import static org.folio.linked.data.util.ResourceUtils.getTypeUris;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.domain.dto.ResourceIdDto;
import org.folio.linked.data.domain.dto.ResourceMarcViewDto;
import org.folio.linked.data.domain.dto.ResourceResponseDto;
import org.folio.linked.data.exception.RequestProcessingException;
import org.folio.linked.data.exception.RequestProcessingExceptionBuilder;
import org.folio.linked.data.integration.client.SrsClient;
import org.folio.linked.data.mapper.ResourceModelMapper;
import org.folio.linked.data.mapper.dto.ResourceMarcViewDtoMapper;
import org.folio.linked.data.mapper.dto.resource.ResourceDtoMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.repo.FolioMetadataRepository;
import org.folio.linked.data.repo.ResourceEdgeRepository;
import org.folio.linked.data.repo.ResourceRepository;
import org.folio.linked.data.service.profile.ResourceProfileLinkingService;
import org.folio.linked.data.service.resource.edge.ResourceEdgeService;
import org.folio.linked.data.service.resource.events.ResourceEventsPublisher;
import org.folio.linked.data.service.resource.graph.ResourceGraphService;
import org.folio.marc4ld.enums.UnmappedMarcHandling;
import org.folio.marc4ld.service.ld2marc.Ld2MarcMapper;
import org.folio.marc4ld.service.marc2ld.bib.MarcBib2ldMapper;
import org.folio.marc4ld.util.TypeUtil;
import org.folio.rest.jaxrs.model.ParsedRecord;
import org.folio.rest.jaxrs.model.Record;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
@Transactional
@RequiredArgsConstructor
public class ResourceMarcBibServiceImpl implements ResourceMarcBibService {

  private final SrsClient srsClient;
  private final ObjectMapper objectMapper;
  private final Ld2MarcMapper ld2MarcMapper;
  private final ResourceRepository resourceRepo;
  private final ResourceEdgeRepository edgeRepo;
  private final MarcBib2ldMapper marcBib2ldMapper;
  private final ResourceMarcViewDtoMapper resourceMarcViewDtoMapper;
  private final ResourceDtoMapper resourceDtoMapper;
  private final ResourceEdgeService resourceEdgeService;
  private final ResourceModelMapper resourceModelMapper;
  private final ResourceGraphService resourceGraphService;
  private final FolioMetadataRepository folioMetadataRepository;
  private final RequestProcessingExceptionBuilder exceptionBuilder;
  private final ResourceEventsPublisher eventsPublisher;
  private final RawMarcService rawMarcService;
  private final ResourceProfileLinkingService resourceProfileLinkingService;

  @Override
  @Transactional(readOnly = true)
  public ResourceMarcViewDto getResourceMarcView(Long id) {
    var resource = resourceRepo.findById(id)
      .orElseThrow(() -> {
        log.error(RESOURCE_WITH_GIVEN_ID + "{}" + IS_NOT_FOUND, id);
        return exceptionBuilder.notFoundLdResourceByIdException("Resource", String.valueOf(id));
      });
    validateMarcViewSupportedType(resource);
    var resourceModel = resourceModelMapper.toModel(resource);
    var marc = ld2MarcMapper.toMarcJson(resourceModel, UnmappedMarcHandling.APPEND);
    return resourceMarcViewDtoMapper.toMarcViewDto(resource, marc);
  }

  @Override
  public boolean checkMarcBibImportableToGraph(String inventoryId) {
    return getRecord(inventoryId)
      .map(HttpEntity::getBody)
      .map(Record::getParsedRecord)
      .map(parsedRecord -> (Map<?, ?>) parsedRecord.getContent())
      .map(content -> (String) content.get("leader"))
      .map(TypeUtil::isSupported)
      .orElseThrow(() -> createSrNotFoundException(inventoryId));
  }

  @Override
  public ResourceResponseDto getResourcePreviewByInventoryId(String inventoryId) {
    var resourceResponseDto = getResource(inventoryId)
      .map(resourceModelMapper::toEntity)
      .map(resourceDtoMapper::toDto)
      .orElseThrow(() -> createSrNotFoundException(inventoryId));
    log.info("Returning resource preview for MARC BIB record with inventory ID: {}", inventoryId);
    return resourceResponseDto;
  }

  @Override
  public ResourceIdDto importMarcRecord(String inventoryId, Integer profileId) {
    var resource = getResource(inventoryId)
      .orElseThrow(() -> createSrNotFoundException(inventoryId));

    resource.getFolioMetadata().setSource(LINKED_DATA);
    var savedResource = saveAndPublishEvents(resource);
    resourceProfileLinkingService.linkResourceToProfile(savedResource, profileId);

    log.info("MARC BIB record with inventory ID: {} is successfully imported to graph resource with ID: {}",
      inventoryId, savedResource.getId());

    return new ResourceIdDto().id(savedResource.getId().toString());
  }

  @Override
  public boolean saveAdminMetadata(org.folio.ld.dictionary.model.Resource modelResource) {
    if (isNull(modelResource.getFolioMetadata())) {
      log.info("Incoming Resource with id [{}] doesn't contain FolioMetadata", modelResource.getId());
      return false;
    }
    var inventoryId = modelResource.getFolioMetadata().getInventoryId();
    if (isNull(inventoryId)) {
      log.info("Incoming Resource with id [{}] doesn't contain Inventory ID", modelResource.getId());
      return false;
    }
    var adminEdge = modelResource.getOutgoingEdges().stream()
      .filter(re -> ADMIN_METADATA.equals(re.getPredicate()))
      .findFirst()
      .orElse(null);
    if (isNull(adminEdge)) {
      log.info("Incoming Resource with id [{}] doesn't contain AdminMetadata", modelResource.getId());
      return false;
    }
    var idOptional = folioMetadataRepository.findIdByInventoryId(inventoryId);
    if (idOptional.isEmpty()) {
      log.info("Resource doesn't exist by Inventory ID [{}]", inventoryId);
      return false;
    }
    var resourceId = idOptional.get().getId();
    removeExistingAdminMetadataIfAny(resourceId);
    var edgeId = resourceEdgeService.saveNewResourceEdge(resourceId, adminEdge.getPredicate(), adminEdge.getTarget());
    log.info("New AdminMetadata has been added and saved under id [{}]", edgeId);
    return true;
  }

  private void validateMarcViewSupportedType(Resource resource) {
    if (resource.isOfType(INSTANCE)) {
      return;
    }
    var type = String.join(", ", getTypeUris(resource));
    log.error("Resource is not supported for MARC view: {}", type);
    throw exceptionBuilder.notSupportedException(type, "MARC view");
  }

  private RequestProcessingException createSrNotFoundException(String inventoryId) {
    log.error(MSG_NOT_FOUND_IN, "Source Record", "inventoryId", inventoryId, "SRS");
    return exceptionBuilder.notFoundSourceRecordException("inventoryId", inventoryId);
  }

  private Optional<ResponseEntity<Record>> getRecord(String inventoryId) {
    try {
      return Optional.of(srsClient.getSourceStorageInstanceRecordById(inventoryId));
    } catch (FeignException.NotFound e) {
      return Optional.empty();
    }
  }

  private Optional<org.folio.ld.dictionary.model.Resource> getResource(String inventoryId) {
    return getRecord(inventoryId)
      .map(HttpEntity::getBody)
      .map(Record::getParsedRecord)
      .map(ParsedRecord::getContent)
      .map(this::toJsonString)
      .flatMap(marcBib2ldMapper::fromMarcJson);
  }

  @SneakyThrows
  private String toJsonString(Object content) {
    return objectMapper.writeValueAsString(content);
  }

  private Resource saveAndPublishEvents(org.folio.ld.dictionary.model.Resource modelResource) {
    ensureNotDuplicate(modelResource);

    var resourceEntity = resourceModelMapper.toEntity(modelResource);
    var saveGraphResult = resourceGraphService.saveMergingGraph(resourceEntity);
    var newResource = saveGraphResult.rootResource();
    refreshWork(newResource);
    saveUnmappedMarc(modelResource, newResource);
    eventsPublisher.emitEventsForUpdate(saveGraphResult);
    return newResource;
  }

  private void ensureNotDuplicate(org.folio.ld.dictionary.model.Resource modelResource) {
    var id = modelResource.getId();
    if (resourceRepo.existsById(id)) {
      log.error("The same resource ID {} already exists", id);
      throw exceptionBuilder.alreadyExistsException("ID", String.valueOf(id));
    }

    var srsId = modelResource.getFolioMetadata().getSrsId();
    if (folioMetadataRepository.existsBySrsId(srsId)) {
      log.error("MARC record having srsID: {} is already imported to data graph", srsId);
      throw exceptionBuilder.alreadyExistsException("srsId", srsId);
    }
  }

  private void saveUnmappedMarc(org.folio.ld.dictionary.model.Resource modelResource, Resource saved) {
    ofNullable(modelResource.getUnmappedMarc())
      .map(org.folio.ld.dictionary.model.RawMarc::getContent)
      .ifPresent(unmappedMarc -> rawMarcService.saveRawMarc(saved, unmappedMarc));
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

  private void removeExistingAdminMetadataIfAny(Long resourceId) {
    var removedEdgesCount = resourceEdgeService.deleteEdgesHavingPredicate(resourceId, ADMIN_METADATA);
    if (removedEdgesCount > 0) {
      log.info("Removed existing adminMetadata edge for resource {}. Count: {}", resourceId, removedEdgesCount);
    }
  }
}
