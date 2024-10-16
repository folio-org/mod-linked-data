package org.folio.linked.data.service.resource;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.ObjectUtils.notEqual;
import static org.folio.linked.data.util.Constants.IS_NOT_FOUND;
import static org.folio.linked.data.util.Constants.RESOURCE_WITH_GIVEN_ID;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.domain.dto.ResourceGraphDto;
import org.folio.linked.data.exception.NotFoundException;
import org.folio.linked.data.mapper.dto.ResourceDtoMapper;
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
  private final ResourceDtoMapper resourceDtoMapper;

  @Override
  public ResourceGraphDto getResourceGraph(Long id) {
    var resource = resourceRepo.findById(id)
      .orElseThrow(() -> new NotFoundException(RESOURCE_WITH_GIVEN_ID + id + IS_NOT_FOUND));
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
      saveEdges(resource, resource.getOutgoingEdges(), ResourceEdge::getTarget, saved);
      saveEdges(resource, resource.getIncomingEdges(), ResourceEdge::getSource, saved);
    }
    return resource;
  }

  private void saveOrUpdate(Resource resource) {
    resourceRepo.findById(resource.getId())
      .ifPresentOrElse(existing -> updateResourceDoc(existing, resource),
        () -> resourceRepo.save(resource)
      );
  }

  private void updateResourceDoc(Resource existing, Resource incoming) {
    resourceRepo.save(existing.setDoc(JsonUtils.merge(existing.getDoc(), incoming.getDoc())));
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
