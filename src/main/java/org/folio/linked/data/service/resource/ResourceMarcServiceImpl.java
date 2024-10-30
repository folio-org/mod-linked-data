package org.folio.linked.data.service.resource;

import static java.util.Objects.isNull;
import static org.folio.ld.dictionary.PredicateDictionary.REPLACED_BY;
import static org.folio.ld.dictionary.PropertyDictionary.RESOURCE_PREFERRED;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.ld.dictionary.model.ResourceSource.LINKED_DATA;
import static org.folio.linked.data.util.BibframeUtils.extractWorkFromInstance;
import static org.folio.linked.data.util.Constants.IS_NOT_FOUND;
import static org.folio.linked.data.util.Constants.RESOURCE_WITH_GIVEN_ID;
import static org.folio.marc4ld.util.MarcUtil.isLanguageMaterial;
import static org.folio.marc4ld.util.MarcUtil.isMonographicComponentPartOrItem;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import org.folio.linked.data.exception.NotFoundException;
import org.folio.linked.data.exception.ValidationException;
import org.folio.linked.data.mapper.ResourceModelMapper;
import org.folio.linked.data.mapper.dto.ResourceDtoMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.model.entity.ResourceTypeEntity;
import org.folio.linked.data.model.entity.event.ResourceCreatedEvent;
import org.folio.linked.data.model.entity.event.ResourceEvent;
import org.folio.linked.data.model.entity.event.ResourceReplacedEvent;
import org.folio.linked.data.model.entity.event.ResourceUpdatedEvent;
import org.folio.linked.data.repo.FolioMetadataRepository;
import org.folio.linked.data.repo.ResourceEdgeRepository;
import org.folio.linked.data.repo.ResourceRepository;
import org.folio.marc4ld.service.ld2marc.Bibframe2MarcMapper;
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
public class ResourceMarcServiceImpl implements ResourceMarcService {

  private final ObjectMapper objectMapper;
  private final ResourceRepository resourceRepo;
  private final ResourceEdgeRepository edgeRepo;
  private final ResourceDtoMapper resourceDtoMapper;
  private final ResourceModelMapper resourceModelMapper;
  private final Bibframe2MarcMapper bibframe2MarcMapper;
  private final MarcBib2ldMapper marcBib2ldMapper;
  private final ResourceGraphService resourceGraphService;
  private final FolioMetadataRepository folioMetadataRepository;
  private final ApplicationEventPublisher applicationEventPublisher;
  private final SrsClient srsClient;


  public Long saveMarcResource(org.folio.ld.dictionary.model.Resource modelResource) {
    var mapped = resourceModelMapper.toEntity(modelResource);
    if (resourceRepo.existsById(modelResource.getId())) {
      return updateResource(mapped);
    }
    if (folioMetadataRepository.existsBySrsId(modelResource.getFolioMetadata().getSrsId())) {
      return replaceResource(mapped);
    }
    return createResource(mapped);
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

  @Override
  public Boolean isSupportedByInventoryId(String inventoryId) {
    return getRecord(inventoryId)
      .map(HttpEntity::getBody)
      .map(Record::getParsedRecord)
      .map(parsedRecord -> (Map<?, ?>) parsedRecord.getContent())
      .map(content -> (String) content.get("leader"))
      .map(this::isMonograph)
      .orElseThrow(() -> createNotFoundException(inventoryId));
  }

  @Override
  public ResourceResponseDto getResourcePreviewByInventoryId(String inventoryId) {
    return getResource(inventoryId)
      .map(resourceModelMapper::toEntity)
      .map(resourceDtoMapper::toDto)
      .orElseThrow(() -> createNotFoundException(inventoryId));
  }

  @Override
  public ResourceIdDto importMarcRecord(String inventoryId) {
    return getResource(inventoryId)
      .map(resource -> {
        resource.getFolioMetadata().setSource(LINKED_DATA);
        return resource;
      })
      .map(this::saveMarcResource)
      .map(String::valueOf)
      .map(id -> new ResourceIdDto().id(id))
      .orElseThrow(() -> createNotFoundException(inventoryId));
  }

  private void validateMarkViewSupportedType(Resource resource) {
    if (resource.isOfType(INSTANCE)) {
      return;
    }
    throw new ValidationException(
      "Resource is not supported for MARC view", "type",
      resource.getTypes().stream()
        .map(ResourceTypeEntity::getUri)
        .collect(Collectors.joining(", ", "[", "]"))
    );
  }

  private Long createResource(Resource resource) {
    logMarcAction(resource, "not found by id and srsId", "be created");
    return saveAndPublishEvent(resource, ResourceCreatedEvent::new);
  }

  private Long replaceResource(Resource resource) {
    if (resource.isAuthority()) {
      return replaceAuthority(resource);
    }
    return replaceBibliographic(resource);
  }

  private Long replaceAuthority(Resource resource) {
    var srsId = resource.getFolioMetadata().getSrsId();
    return resourceRepo.findByFolioMetadataSrsId(srsId)
      .map(previous -> {
        var previousObsolete = markObsolete(previous);
        setPreferred(resource, true);
        var re = new ResourceEdge(previousObsolete, resource, REPLACED_BY);
        previousObsolete.addOutgoingEdge(re);
        resource.addIncomingEdge(re);
        logMarcAction(resource, "not found by id, but found by srsId [" + srsId + "]",
          "be saved as a new version of previously existed resource [id " + previous.getId() + "]");
        return saveAndPublishEvent(resource, saved -> new ResourceReplacedEvent(previousObsolete, saved));
      })
      .orElseThrow(() -> new NotFoundException("Resource not found by srsId: " + srsId));
  }

  private Resource markObsolete(Resource resource) {
    resource.setActive(false);
    setPreferred(resource, false);
    resource.setFolioMetadata(null);
    return resource;
  }

  private void setPreferred(Resource resource, boolean preferred) {
    if (isNull(resource.getDoc())) {
      resource.setDoc(objectMapper.createObjectNode());
    }
    var arrayNode = objectMapper.createArrayNode().add(String.valueOf(preferred));
    ((ObjectNode) resource.getDoc()).set(RESOURCE_PREFERRED.getValue(), arrayNode);
  }

  private Long replaceBibliographic(Resource resource) {
    var srsId = resource.getFolioMetadata().getSrsId();
    return resourceRepo.findByFolioMetadataSrsId(srsId)
      .map(Resource::new)
      .map(existedBySrsId -> {
        logMarcAction(resource, "not found by id, but found by srsId [" + srsId + "]",
          "replace previously existed [id " + existedBySrsId.getId() + "]");
        return saveAndPublishEvent(resource, saved -> new ResourceReplacedEvent(existedBySrsId, saved));
      })
      .orElseThrow(() -> new NotFoundException("Resource not found by srsId: " + srsId));
  }

  private Long updateResource(Resource resource) {
    var id = resource.getId();
    var srsId = resource.getFolioMetadata().getSrsId();
    logMarcAction(resource, "found by id [" + id + "] with srsId [" + srsId + "]", "be updated");
    return saveAndPublishEvent(resource, ResourceUpdatedEvent::new);
  }

  private void logMarcAction(Resource resource, String existence, String action) {
    log.info("Incoming {} resource [id {}, srsId {}] is {} and will {}",
      getResourceKind(resource), resource.getId(), resource.getFolioMetadata().getSrsId(), existence, action);
  }

  private String getResourceKind(Resource resource) {
    if (resource.isAuthority()) {
      return "Authority";
    }
    return "Bibliographic";
  }

  private Long saveAndPublishEvent(Resource resource, Function<Resource, ResourceEvent> resourceEventSupplier) {
    var newResource = resourceGraphService.saveMergingGraph(resource);
    refreshWork(newResource);
    var event = resourceEventSupplier.apply(newResource);
    if (event instanceof ResourceReplacedEvent rre) {
      if (resource.isAuthority()) {
        resourceRepo.save(rre.previous());
      } else {
        resourceGraphService.breakEdgesAndDelete(rre.previous());
      }
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

  private boolean isMonograph(String leader) {
    return isLanguageMaterial(leader.charAt(6)) && isMonographicComponentPartOrItem(leader.charAt(7));
  }

  private Optional<ResponseEntity<Record>> getRecord(String inventoryId) {
    try {
      return Optional.of(srsClient.getFormattedSourceStorageInstanceRecordById(inventoryId));
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

  private NotFoundException createNotFoundException(String inventoryId) {
    return new NotFoundException(String.format("Record with INSTANCE id: %s was not found", inventoryId));
  }
}
