package org.folio.linked.data.service.resource;

import static java.util.Collections.emptySet;
import static org.folio.ld.dictionary.PropertyDictionary.CITATION_COVERAGE;
import static org.folio.ld.dictionary.PropertyDictionary.CREDITS_NOTE;
import static org.folio.ld.dictionary.PropertyDictionary.DATES_OF_PUBLICATION_NOTE;
import static org.folio.ld.dictionary.PropertyDictionary.GEOGRAPHIC_COVERAGE;
import static org.folio.ld.dictionary.PropertyDictionary.GOVERNING_ACCESS_NOTE;
import static org.folio.ld.dictionary.PropertyDictionary.LOCATION_OF_ORIGINALS_DUPLICATES;
import static org.folio.ld.dictionary.PropertyDictionary.OTHER_EVENT_INFORMATION;
import static org.folio.ld.dictionary.PropertyDictionary.PARTICIPANT_NOTE;
import static org.folio.ld.dictionary.PropertyDictionary.PUBLICATION_FREQUENCY;
import static org.folio.ld.dictionary.PropertyDictionary.REFERENCES;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.folio.linked.data.util.ResourceUtils.getPrimaryMainTitles;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.domain.dto.InstanceField;
import org.folio.linked.data.domain.dto.ResourceIdDto;
import org.folio.linked.data.domain.dto.ResourceRequestDto;
import org.folio.linked.data.domain.dto.ResourceResponseDto;
import org.folio.linked.data.domain.dto.WorkField;
import org.folio.linked.data.exception.RequestProcessingExceptionBuilder;
import org.folio.linked.data.mapper.dto.ResourceDtoMapper;
import org.folio.linked.data.model.entity.RawMarc;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.event.ResourceCreatedEvent;
import org.folio.linked.data.model.entity.event.ResourceDeletedEvent;
import org.folio.linked.data.model.entity.event.ResourceReplacedEvent;
import org.folio.linked.data.model.entity.event.ResourceUpdatedEvent;
import org.folio.linked.data.repo.FolioMetadataRepository;
import org.folio.linked.data.repo.ResourceRepository;
import org.folio.linked.data.service.resource.edge.ResourceEdgeService;
import org.folio.linked.data.service.resource.graph.ResourceGraphService;
import org.folio.linked.data.service.resource.meta.MetadataService;
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

  private static final Map<String, Set<String>> PROPERTIES_TO_BE_COPIED = Map.of(
    INSTANCE.getUri(), Set.of(
      PUBLICATION_FREQUENCY.getValue(),
      DATES_OF_PUBLICATION_NOTE.getValue(),
      GOVERNING_ACCESS_NOTE.getValue(),
      CREDITS_NOTE.getValue(),
      PARTICIPANT_NOTE.getValue(),
      CITATION_COVERAGE.getValue(),
      LOCATION_OF_ORIGINALS_DUPLICATES.getValue()
    ),
    WORK.getUri(), Set.of(
      REFERENCES.getValue(),
      OTHER_EVENT_INFORMATION.getValue(),
      GEOGRAPHIC_COVERAGE.getValue()
    )
  );

  private final ResourceRepository resourceRepo;
  private final MetadataService metadataService;
  private final ResourceDtoMapper resourceDtoMapper;
  private final ResourceGraphService resourceGraphService;
  private final FolioMetadataRepository folioMetadataRepo;
  private final RequestProcessingExceptionBuilder exceptionBuilder;
  private final ApplicationEventPublisher applicationEventPublisher;
  private final ResourceEdgeService resourceEdgeService;
  private final FolioExecutionContext folioExecutionContext;
  private final ObjectMapper objectMapper;

  @Override
  public ResourceResponseDto createResource(ResourceRequestDto resourceDto) {
    log.info("Received request to create new resource - {}", toLogString(resourceDto));
    var mapped = resourceDtoMapper.toEntity(resourceDto);
    log.debug("createResource\n[{}]\nfrom DTO [{}]", mapped, resourceDto);
    metadataService.ensure(mapped);
    var persisted = resourceGraphService.saveMergingGraph(mapped);
    applicationEventPublisher.publishEvent(new ResourceCreatedEvent(persisted));
    return resourceDtoMapper.toDto(persisted);
  }

  @Override
  @Transactional(readOnly = true)
  public ResourceResponseDto getResourceById(Long id) {
    var resource = getResource(id);
    return resourceDtoMapper.toDto(resource);
  }

  @Override
  public ResourceIdDto getResourceIdByInventoryId(String inventoryId) {
    return folioMetadataRepo.findIdByInventoryId(inventoryId)
      .map(idOnly -> new ResourceIdDto().id(String.valueOf(idOnly.getId())))
      .orElseThrow(() -> exceptionBuilder.notFoundLdResourceByInventoryIdException(inventoryId));
  }

  @Override
  public ResourceResponseDto updateResource(Long id, ResourceRequestDto resourceDto) {
    log.info("Received request to update resource {} - {}", id, toLogString(resourceDto));
    log.debug("updateResource [{}] from DTO [{}]", id, resourceDto);
    var existed = getResource(id);
    var oldResource = new Resource(existed);
    resourceGraphService.breakEdgesAndDelete(existed);
    var newResource = saveNewResource(resourceDto, oldResource);
    if (Objects.equals(oldResource.getId(), newResource.getId())) {
      applicationEventPublisher.publishEvent(new ResourceUpdatedEvent(newResource));
    } else {
      applicationEventPublisher.publishEvent(new ResourceReplacedEvent(oldResource, newResource.getId()));
    }
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

  private Resource getResource(Long id) {
    return resourceRepo.findById(id)
      .orElseThrow(() -> exceptionBuilder.notFoundLdResourceByIdException("Resource", String.valueOf(id)));
  }

  private Resource saveNewResource(ResourceRequestDto resourceDto, Resource old) {
    var mapped = resourceDtoMapper.toEntity(resourceDto);
    resourceEdgeService.copyOutgoingEdges(old, mapped);
    metadataService.ensure(mapped, old.getFolioMetadata());
    copyUnmappedMarc(old, mapped);
    copyProperties(old, mapped);
    mapped.setCreatedDate(old.getCreatedDate());
    mapped.setVersion(old.getVersion() + 1);
    mapped.setCreatedBy(old.getCreatedBy());
    mapped.setUpdatedBy(folioExecutionContext.getUserId());
    return resourceGraphService.saveMergingGraph(mapped);
  }

  private String toLogString(ResourceRequestDto resourceDto) {
    String type = "-";
    List<String> titles = List.of();
    if (resourceDto.getResource() instanceof InstanceField instanceField) {
      type = "Instance";
      titles = getPrimaryMainTitles(instanceField.getInstance().getTitle());
    } else if (resourceDto.getResource() instanceof WorkField workField) {
      type = "Work";
      titles = getPrimaryMainTitles(workField.getWork().getTitle());
    }
    return String.format("Type: %s, Title: %s", type, titles);
  }

  private void copyUnmappedMarc(Resource old, Resource mapped) {
    if (mapped.isOfType(INSTANCE)) {
      Optional.ofNullable(old.getUnmappedMarc())
        .ifPresent(unmappedMarc -> {
          var newUnmappedMarc = new RawMarc(mapped).setContent(unmappedMarc.getContent());
          mapped.setUnmappedMarc(newUnmappedMarc);
        });
    }
  }

  @SneakyThrows
  private void copyProperties(Resource from, Resource to) {
    if (from.getDoc() == null) {
      return;
    }
    var properties = getProperties(from);
    if (!properties.isEmpty()) {
      var toDoc = to.getDoc() == null
        ? new HashMap<String, List<String>>()
        : objectMapper.treeToValue(to.getDoc(), new TypeReference<HashMap<String, List<String>>>() {});
      properties.forEach(entry -> toDoc.put(entry.getKey(), entry.getValue()));
      to.setDoc(objectMapper.convertValue(toDoc, JsonNode.class));
    }
  }

  private List<Map.Entry<String, List<String>>> getProperties(Resource from) throws JsonProcessingException {
    var fromDoc = objectMapper.treeToValue(from.getDoc(), new TypeReference<HashMap<String, List<String>>>() {});
    var fromType = from.getTypes()
      .stream()
      .findFirst()
      .orElseThrow()
      .getUri();
    return fromDoc.entrySet()
      .stream()
      .filter(entry -> PROPERTIES_TO_BE_COPIED.getOrDefault(fromType, emptySet()).contains(entry.getKey()))
      .toList();
  }

}
