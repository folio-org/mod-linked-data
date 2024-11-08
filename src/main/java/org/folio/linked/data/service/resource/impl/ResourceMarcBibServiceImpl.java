package org.folio.linked.data.service.resource.impl;

import static java.lang.String.format;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.ld.dictionary.model.ResourceSource.LINKED_DATA;
import static org.folio.linked.data.util.BibframeUtils.extractWorkFromInstance;
import static org.folio.linked.data.util.Constants.IS_NOT_FOUND;
import static org.folio.linked.data.util.Constants.RESOURCE_WITH_GIVEN_ID;
import static org.folio.marc4ld.util.MarcUtil.isLanguageMaterial;
import static org.folio.marc4ld.util.MarcUtil.isMonographicComponentPartOrItem;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.client.SrsClient;
import org.folio.linked.data.domain.dto.ResourceIdDto;
import org.folio.linked.data.domain.dto.ResourceMarcViewDto;
import org.folio.linked.data.domain.dto.ResourceResponseDto;
import org.folio.linked.data.exception.AlreadyExistsException;
import org.folio.linked.data.exception.NotFoundException;
import org.folio.linked.data.exception.ValidationException;
import org.folio.linked.data.mapper.ResourceModelMapper;
import org.folio.linked.data.mapper.dto.ResourceDtoMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceTypeEntity;
import org.folio.linked.data.model.entity.event.ResourceEvent;
import org.folio.linked.data.model.entity.event.ResourceReplacedEvent;
import org.folio.linked.data.model.entity.event.ResourceUpdatedEvent;
import org.folio.linked.data.repo.FolioMetadataRepository;
import org.folio.linked.data.repo.ResourceEdgeRepository;
import org.folio.linked.data.repo.ResourceRepository;
import org.folio.linked.data.service.resource.ResourceGraphService;
import org.folio.linked.data.service.resource.ResourceMarcBibService;
import org.folio.marc4ld.service.ld2marc.Ld2MarcMapper;
import org.folio.marc4ld.service.marc2ld.bib.MarcBib2ldMapper;
import org.folio.rest.jaxrs.model.ParsedRecord;
import org.folio.rest.jaxrs.model.Record;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
@Transactional
@RequiredArgsConstructor
public class ResourceMarcBibServiceImpl implements ResourceMarcBibService {

  private static final String RECORD_NOT_FOUND_BY_INVENTORY_ID = "Record with inventoryId: %s was not found";

  private final ObjectMapper objectMapper;
  private final ResourceRepository resourceRepo;
  private final ResourceEdgeRepository edgeRepo;
  private final ResourceDtoMapper resourceDtoMapper;
  private final ResourceModelMapper resourceModelMapper;
  private final Ld2MarcMapper ld2MarcMapper;
  private final MarcBib2ldMapper marcBib2ldMapper;
  private final ResourceGraphService resourceGraphService;
  private final FolioMetadataRepository folioMetadataRepository;
  private final ApplicationEventPublisher applicationEventPublisher;
  private final SrsClient srsClient;

  @Override
  @Transactional(readOnly = true)
  public ResourceMarcViewDto getResourceMarcView(Long id) {
    var resource = resourceRepo.findById(id)
      .orElseThrow(() -> createNotFoundException(RESOURCE_WITH_GIVEN_ID + id + IS_NOT_FOUND));
    validateMarkViewSupportedType(resource);
    var resourceModel = resourceModelMapper.toModel(resource);
    var marc = ld2MarcMapper.toMarcJson(resourceModel);
    return resourceDtoMapper.toMarcViewDto(resource, marc);
  }

  @Override
  public Boolean isSupportedByInventoryId(String inventoryId) {
    return getRecord(inventoryId)
      .map(HttpEntity::getBody)
      .map(Record::getParsedRecord)
      .map(parsedRecord -> (Map<?, ?>) parsedRecord.getContent())
      .map(content -> (String) content.get("leader"))
      .map(this::isMonograph)
      .orElseThrow(() -> createNotFoundException(format(RECORD_NOT_FOUND_BY_INVENTORY_ID, inventoryId)));
  }

  @Override
  public ResourceResponseDto getResourcePreviewByInventoryId(String inventoryId) {
    var resourceResponseDto = getResource(inventoryId)
      .map(resourceModelMapper::toEntity)
      .map(resourceDtoMapper::toDto)
      .orElseThrow(() -> createNotFoundException(format(RECORD_NOT_FOUND_BY_INVENTORY_ID, inventoryId)));
    log.info("Returning resource preview for MARC BIB record with inventory ID: {}", inventoryId);
    return resourceResponseDto;
  }

  @Override
  public ResourceIdDto importMarcRecord(String inventoryId) {
    var resourceIdDto = getResource(inventoryId)
      .map(resource -> {
        resource.getFolioMetadata().setSource(LINKED_DATA);
        return resource;
      })
      .map(this::save)
      .map(String::valueOf)
      .map(id -> new ResourceIdDto().id(id))
      .orElseThrow(() -> createNotFoundException(format(RECORD_NOT_FOUND_BY_INVENTORY_ID, inventoryId)));
    log.info("MARC BIB record with inventory ID: {} is successfully imported to graph resource with ID: {}",
      inventoryId, resourceIdDto.getId());
    return resourceIdDto;
  }

  private void validateMarkViewSupportedType(Resource resource) {
    if (resource.isOfType(INSTANCE)) {
      return;
    }
    var message = "Resource is not supported for MARC view";
    log.error(message);
    throw new ValidationException(
      message, "type",
      resource.getTypes().stream()
        .map(ResourceTypeEntity::getUri)
        .collect(Collectors.joining(", ", "[", "]"))
    );
  }

  private NotFoundException createNotFoundException(String message) {
    log.error(message);
    return new NotFoundException(message);
  }

  private Optional<ResponseEntity<Record>> getRecord(String inventoryId) {
    try {
      return Optional.of(srsClient.getFormattedSourceStorageInstanceRecordById(inventoryId));
    } catch (FeignException.NotFound e) {
      return Optional.empty();
    }
  }

  private boolean isMonograph(String leader) {
    return isLanguageMaterial(leader.charAt(6)) && isMonographicComponentPartOrItem(leader.charAt(7));
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

  private Long save(org.folio.ld.dictionary.model.Resource modelResource) {
    var id = modelResource.getId();
    if (resourceRepo.existsById(id)) {
      var message = format("Another resource with ID: %s already exists in the graph", id);
      log.error(message);
      throw new AlreadyExistsException(message);
    }

    var srsId = modelResource.getFolioMetadata().getSrsId();
    if (folioMetadataRepository.existsBySrsId(srsId)) {
      var message = format("MARC record having srsID: %s is already imported to data graph", srsId);
      log.error(message);
      throw new AlreadyExistsException(message);
    }

    // Emitting a ResourceCreatedEvent will send a "CREATE_INSTANCE" Kafka event to Inventory,
    // resulting in a duplicate instance record in Inventory. By emitting a ResourceUpdatedEvent instead,
    // we send an "UPDATE_INSTANCE" event, switching the source of the existing instance
    // from "MARC" to "LINKED_DATA" in Inventory.
    return saveAndPublishEvent(resourceModelMapper.toEntity(modelResource), ResourceUpdatedEvent::new);
  }

  private Long saveAndPublishEvent(Resource resource, Function<Resource, ResourceEvent> resourceEventSupplier) {
    var newResource = resourceGraphService.saveMergingGraph(resource);
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
