package org.folio.linked.data.service.resource.graph;

import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.ObjectUtils.notEqual;
import static org.folio.ld.dictionary.PredicateDictionary.REPLACED_BY;
import static org.folio.ld.dictionary.PropertyDictionary.RESOURCE_PREFERRED;
import static org.folio.linked.data.util.ResourceUtils.isPreferred;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.domain.dto.ResourceGraphDto;
import org.folio.linked.data.exception.RequestProcessingExceptionBuilder;
import org.folio.linked.data.mapper.dto.ResourceGraphDtoMapper;
import org.folio.linked.data.model.entity.FolioMetadata;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.repo.ResourceEdgeRepository;
import org.folio.linked.data.repo.ResourceRepository;
import org.folio.linked.data.util.JsonUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
@Transactional
@RequiredArgsConstructor
public class ResourceGraphServiceImpl implements ResourceGraphService {

  private final ResourceRepository resourceRepo;
  private final ResourceEdgeRepository edgeRepo;
  private final ResourceGraphDtoMapper resourceDtoMapper;
  private final RequestProcessingExceptionBuilder exceptionBuilder;

  @Override
  public ResourceGraphDto getResourceGraph(Long id) {
    var resource = resourceRepo.findById(id)
      .orElseThrow(() -> exceptionBuilder.notFoundLdResourceByIdException("Resource", String.valueOf(id)));
    return resourceDtoMapper.toResourceGraphDto(resource);
  }

  @Override
  public SaveGraphResult saveMergingGraph(Resource resource) {
    Set<ResourceSaveResult> results = saveMergingGraphSkippingAlreadySaved(resource, null);

    var createdResources = filterResources(results, ResourceSaveResult::isCreated);
    var updatedResources = filterResources(results, ResourceSaveResult::isUpdated);

    return new SaveGraphResult(resource, createdResources, updatedResources);
  }

  @Override
  public void breakEdgesAndDelete(Resource resource) {
    breakCircularEdges(resource);
    resourceRepo.delete(resource);
  }

  private Set<ResourceSaveResult> saveMergingGraphSkippingAlreadySaved(Resource resource, Resource saved) {
    var result = new LinkedHashSet<ResourceSaveResult>();
    if (resource.isNew()) {
      result.add(saveOrUpdate(resource));
      result.addAll(saveEdges(resource, resource.getOutgoingEdges(), ResourceEdge::getTarget, saved));
      result.addAll(saveEdges(resource, resource.getIncomingEdges(), ResourceEdge::getSource, saved));
    }
    return result;
  }

  private ResourceSaveResult saveOrUpdate(Resource resource) {
    return resourceRepo.findById(resource.getId())
      .map(existing -> updateResource(existing, resource))
      .orElseGet(() -> ResourceSaveResult.created(resourceRepo.save(resource)));
  }

  private ResourceSaveResult updateResource(Resource existingResource, Resource incomingResource) {
    existingResource.setDoc(mergeDocs(existingResource, incomingResource));
    existingResource.setActive(incomingResource.isActive());
    addFolioMetadataIfAbsent(existingResource, incomingResource.getFolioMetadata());
    removeReplacedByEdgeIfPreferred(existingResource);
    return ResourceSaveResult.updated(existingResource);
  }

  private void addFolioMetadataIfAbsent(Resource existingResource, FolioMetadata incomingFolioMetadata) {
    if (nonNull(existingResource.getFolioMetadata())) {
      return;
    }
    ofNullable(incomingFolioMetadata)
      .ifPresent(folioMetadata -> existingResource.setFolioMetadata(
        folioMetadata.toBuilder()
          .resource(existingResource)
          .build()
      ));
  }

  private JsonNode mergeDocs(Resource existingResource, Resource incomingResource) {
    var mergedDoc = JsonUtils.merge(existingResource.getDoc(), incomingResource.getDoc());
    resetPreferredFlagWithIncomingValue(incomingResource, mergedDoc);
    return mergedDoc;
  }

  private static void resetPreferredFlagWithIncomingValue(Resource incomingResource, JsonNode mergedDoc) {
    var resourcePreferred = RESOURCE_PREFERRED.getValue();
    ofNullable(incomingResource.getDoc())
      .flatMap(incomingDoc -> JsonUtils.getProperty(incomingDoc, resourcePreferred))
      .ifPresent(incomingPreferredFlag -> JsonUtils.setProperty(mergedDoc, resourcePreferred, incomingPreferredFlag));
  }

  private void removeReplacedByEdgeIfPreferred(Resource resource) {
    if (isPreferred(resource)) {
      resource.getOutgoingEdges().removeIf(edge -> edge.getPredicate().getUri().equals(REPLACED_BY.getUri()));
    }
  }

  private Set<ResourceSaveResult> saveEdges(Resource resource,
                                            Set<ResourceEdge> edges,
                                            Function<ResourceEdge, Resource> resourceSelector,
                                            Resource saved) {
    return edges.stream()
      .filter(edge -> notEqual(resourceSelector.apply(edge), saved))
      .flatMap(edge -> saveEdge(resourceSelector.apply(edge), resource, edge).stream())
      .collect(Collectors.toSet());
  }

  private Set<ResourceSaveResult> saveEdge(Resource edgeResource, Resource resource, ResourceEdge edge) {
    Set<ResourceSaveResult> resourceSaveResult = new LinkedHashSet<>();
    if (edge.isNew()) {
      resourceSaveResult = saveMergingGraphSkippingAlreadySaved(edgeResource, resource);
      edge.computeId();
      if (doesNotExists(edge)) {
        edgeRepo.save(edge);
      }
    }
    return resourceSaveResult;
  }

  private boolean doesNotExists(ResourceEdge edge) {
    return ofNullable(edge)
      .map(ResourceEdge::getId)
      .map(id -> !edgeRepo.existsById(id))
      .orElse(false);
  }

  private void breakCircularEdges(Resource resource) {
    breakOutgoingCircularEdges(resource);
    breakIncomingCircularEdges(resource);
  }

  private void breakOutgoingCircularEdges(Resource resource) {
    resource.getOutgoingEdges().forEach(edge -> {
      var filtered = edge.getTarget().getIncomingEdges().stream()
        .filter(e -> resource.equals(e.getSource()))
        .collect(Collectors.toSet());
      edge.getTarget().getIncomingEdges().removeAll(filtered);
    });
  }

  private void breakIncomingCircularEdges(Resource resource) {
    resource.getIncomingEdges().forEach(edge -> {
      var filtered = edge.getSource().getOutgoingEdges().stream()
        .filter(e -> resource.equals(e.getTarget()))
        .collect(Collectors.toSet());
      edge.getSource().getOutgoingEdges().removeAll(filtered);
    });
  }

  private Set<Resource> filterResources(Set<ResourceSaveResult> results, Predicate<ResourceSaveResult> predicate) {
    return results.stream()
      .filter(predicate)
      .map(ResourceSaveResult::resource)
      .collect(Collectors.toSet());
  }

  record ResourceSaveResult(Resource resource, boolean isCreated) {
    public static ResourceSaveResult created(Resource resource) {
      return new ResourceSaveResult(resource, true);
    }

    public static ResourceSaveResult updated(Resource resource) {
      return new ResourceSaveResult(resource, false);
    }

    public boolean isUpdated() {
      return !isCreated;
    }
  }
}
