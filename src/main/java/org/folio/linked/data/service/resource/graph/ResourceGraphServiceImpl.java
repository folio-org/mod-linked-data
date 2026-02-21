package org.folio.linked.data.service.resource.graph;

import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toCollection;
import static org.apache.commons.lang3.ObjectUtils.notEqual;
import static org.folio.ld.dictionary.PredicateDictionary.REPLACED_BY;
import static org.folio.ld.dictionary.PropertyDictionary.RESOURCE_PREFERRED;
import static org.folio.linked.data.util.ResourceUtils.isPreferred;
import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.linked.data.exception.RequestProcessingExceptionBuilder;
import org.folio.linked.data.mapper.ResourceModelMapper;
import org.folio.linked.data.model.entity.FolioMetadata;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.repo.ResourceEdgeRepository;
import org.folio.linked.data.repo.ResourceRepository;
import org.folio.linked.data.service.resource.events.ResourceEventsPublisher;
import org.folio.linked.data.util.JsonUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;

@Log4j2
@Service
@Transactional
@RequiredArgsConstructor
public class ResourceGraphServiceImpl implements ResourceGraphService {

  private final ResourceRepository resourceRepo;
  private final ResourceEdgeRepository edgeRepo;
  private final ResourceModelMapper resourceModelMapper;
  private final ResourceEventsPublisher resourceEventsPublisher;
  private final RequestProcessingExceptionBuilder exceptionBuilder;

  @Override
  public org.folio.ld.dictionary.model.Resource getResourceGraph(Long id) {
    var resource = resourceRepo.findById(id)
      .orElseThrow(() -> exceptionBuilder.notFoundLdResourceByIdException("Resource", String.valueOf(id)));
    var modelWithNoIncomingEdges = resourceModelMapper.toModel(resource);
    return withIncomingEdges(modelWithNoIncomingEdges, resource);
  }

  private org.folio.ld.dictionary.model.Resource withIncomingEdges(org.folio.ld.dictionary.model.Resource model,
                                                                   Resource resource) {
    var ies = resource.getIncomingEdges().stream()
      .map(ie -> {
        var sourceModel = new org.folio.ld.dictionary.model.Resource().setId(ie.getSource().getId());
        var predicateModel = PredicateDictionary.fromUri(ie.getPredicate().getUri()).orElseThrow();
        return new org.folio.ld.dictionary.model.ResourceEdge(sourceModel, model, predicateModel);
      })
      .collect(toCollection(LinkedHashSet::new));
    model.setIncomingEdges(ies);
    return model;
  }

  @Override
  @Transactional(propagation = REQUIRES_NEW)
  public SaveGraphResult saveMergingGraphInNewTransaction(Resource resource) {
    var result = saveMergingGraph(resource);
    resourceEventsPublisher.emitEventsForCreateAndUpdate(result, null);
    return result;
  }

  public SaveGraphResult saveMergingGraph(Resource resource) {
    var existingResourcesById = findExistingResources(resource);
    var result = saveMergingGraphSkippingAlreadySaved(resource, null, existingResourcesById);

    var createdResources = filterResources(result, ResourceSaveResult::isCreated);
    var updatedResources = filterResources(result, ResourceSaveResult::isUpdated);
    var persistedRootResource = createdResources.stream()
      .filter(r -> r.getId().equals(resource.getId()))
      .findFirst()
      .orElse(resource);

    return new SaveGraphResult(persistedRootResource, createdResources, updatedResources);
  }

  @Override
  public void breakEdgesAndDelete(Resource resource) {
    breakCircularEdges(resource);
    resourceRepo.delete(resource);
  }

  private Set<ResourceSaveResult> saveMergingGraphSkippingAlreadySaved(Resource resource,
                                                                        Resource saved,
                                                                        Map<Long, Resource> existingResourcesById) {
    var result = new LinkedHashSet<ResourceSaveResult>();
    if (resource.isNew()) {
      result.add(saveOrUpdate(resource, existingResourcesById));
      result.addAll(saveEdges(resource, resource.getOutgoingEdges(), ResourceEdge::getTarget, saved,
        existingResourcesById));
      result.addAll(saveEdges(resource, resource.getIncomingEdges(), ResourceEdge::getSource, saved,
        existingResourcesById));
    }
    return result;
  }

  private ResourceSaveResult saveOrUpdate(Resource resource, Map<Long, Resource> existingResourcesById) {
    return ofNullable(existingResourcesById.get(resource.getId()))
      .map(existing -> {
        var updated = updateResource(existing, resource);
        existingResourcesById.put(updated.resource().getId(), updated.resource());
        return updated;
      })
      .orElseGet(() -> {
        var created = resourceRepo.save(resource);
        existingResourcesById.put(created.getId(), created);
        return ResourceSaveResult.created(created);
      });
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

  private void resetPreferredFlagWithIncomingValue(Resource incomingResource, JsonNode mergedDoc) {
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
                                            Resource saved,
                                            Map<Long, Resource> existingResourcesById) {
    return edges.stream()
      .filter(edge -> notEqual(resourceSelector.apply(edge), saved))
      .flatMap(edge -> saveEdge(resourceSelector.apply(edge), resource, edge, existingResourcesById).stream())
      .collect(Collectors.toSet());
  }

  private Set<ResourceSaveResult> saveEdge(Resource edgeResource,
                                           Resource resource,
                                           ResourceEdge edge,
                                           Map<Long, Resource> existingResourcesById) {
    Set<ResourceSaveResult> resourceSaveResult = new LinkedHashSet<>();
    if (edge.isNew()) {
      resourceSaveResult = saveMergingGraphSkippingAlreadySaved(edgeResource, resource, existingResourcesById);
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

  private Map<Long, Resource> findExistingResources(Resource resource) {
    var candidateIds = new LinkedHashSet<Long>();
    collectNewResourceIds(resource, candidateIds);
    if (candidateIds.isEmpty()) {
      return Map.of();
    }
    return resourceRepo.findAllById(candidateIds).stream()
      .collect(Collectors.toMap(Resource::getId, Function.identity()));
  }

  private void collectNewResourceIds(Resource resource, Set<Long> candidateIds) {
    if (!resource.isNew() || !candidateIds.add(resource.getId())) {
      return;
    }
    resource.getOutgoingEdges().stream()
      .map(ResourceEdge::getTarget)
      .forEach(target -> collectNewResourceIds(target, candidateIds));
    resource.getIncomingEdges().stream()
      .map(ResourceEdge::getSource)
      .forEach(source -> collectNewResourceIds(source, candidateIds));
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
