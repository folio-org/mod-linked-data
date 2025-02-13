package org.folio.linked.data.service.resource.graph;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.ObjectUtils.notEqual;
import static org.folio.ld.dictionary.PredicateDictionary.REPLACED_BY;
import static org.folio.ld.dictionary.PropertyDictionary.RESOURCE_PREFERRED;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.domain.dto.ResourceGraphDto;
import org.folio.linked.data.exception.RequestProcessingExceptionBuilder;
import org.folio.linked.data.mapper.dto.ResourceDtoMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.repo.ResourceEdgeRepository;
import org.folio.linked.data.repo.ResourceRepository;
import org.folio.linked.data.util.JsonUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
@Transactional
public class ResourceGraphServiceImpl implements ResourceGraphService {

  private final ResourceRepository resourceRepo;
  private final ResourceEdgeRepository edgeRepo;
  private final ResourceDtoMapper resourceDtoMapper;
  private final RequestProcessingExceptionBuilder exceptionBuilder;

  public ResourceGraphServiceImpl(ResourceRepository resourceRepo,
                                  ResourceEdgeRepository edgeRepo,
                                  @Lazy ResourceDtoMapper resourceDtoMapper,
                                  RequestProcessingExceptionBuilder exceptionBuilder) {
    this.resourceRepo = resourceRepo;
    this.edgeRepo = edgeRepo;
    this.resourceDtoMapper = resourceDtoMapper;
    this.exceptionBuilder = exceptionBuilder;
  }

  @Override
  public ResourceGraphDto getResourceGraph(Long id) {
    var resource = resourceRepo.findById(id)
      .orElseThrow(() -> exceptionBuilder.notFoundLdResourceByIdException("Resource", String.valueOf(id)));
    return resourceDtoMapper.toResourceGraphDto(resource);
  }

  @Override
  public Resource saveMergingGraph(Resource resource) {
    return saveMergingGraphSkippingAlreadySaved(resource, null);
  }

  @Override
  public void breakEdgesAndDelete(Resource resource) {
    resource.setActive(false);
    resource.setFolioMetadata(null);
    breakCircularEdges(resource);
    resourceRepo.delete(resource);
  }

  private Resource saveMergingGraphSkippingAlreadySaved(Resource resource, Resource saved) {
    if (resource.isNew()) {
      saveOrUpdate(resource);
      if (isPreferred(resource)) {
        // remove "replacedBy" outgoing edge if the current resource is a preferred one
        resource.getOutgoingEdges().removeIf(edge -> edge.getPredicate().getUri().equals(REPLACED_BY.getUri()));
      }
      saveEdges(resource, resource.getOutgoingEdges(), ResourceEdge::getTarget, saved);
      saveEdges(resource, resource.getIncomingEdges(), ResourceEdge::getSource, saved);
    }
    return resource;
  }

  private void saveOrUpdate(Resource resource) {
    resourceRepo.findById(resource.getId())
      .ifPresentOrElse(existing -> updateResource(existing, resource),
        () -> resourceRepo.save(resource)
      );
  }

  private void updateResource(Resource existing, Resource incoming) {
    var resourceToSave = existing.setDoc(JsonUtils.merge(existing.getDoc(), incoming.getDoc()));
    resourceToSave.setActive(incoming.isActive());
    resourceRepo.save(resourceToSave);
  }

  private boolean isPreferred(Resource resource) {
    return Optional.ofNullable(resource.getDoc())
      .map(doc -> doc.get(RESOURCE_PREFERRED.getValue()))
      .filter(JsonNode::isArray)
      .filter(preferredNode -> preferredNode.get(0).asText().equals("true"))
      .isPresent();
  }

  private void saveEdges(Resource resource, Set<ResourceEdge> edges, Function<ResourceEdge, Resource> resourceSelector,
                         Resource saved) {
    edges.stream()
      .filter(edge -> notEqual(resourceSelector.apply(edge), saved))
      .forEach(edge -> saveEdge(resourceSelector.apply(edge), resource, edge));
  }

  private void saveEdge(Resource edgeResource, Resource resource, ResourceEdge edge) {
    if (edge.isNew()) {
      saveMergingGraphSkippingAlreadySaved(edgeResource, resource);
      edge.computeId();
      if (doesNotExists(edge)) {
        edgeRepo.save(edge);
      }
    }
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
}
