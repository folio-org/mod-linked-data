package org.folio.linked.data.test.resource;

import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;
import java.util.stream.Stream;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.model.entity.pk.ResourceEdgePk;
import org.folio.linked.data.repo.ResourceEdgeRepository;
import org.folio.linked.data.repo.ResourceRepository;
import org.folio.linked.data.service.resource.graph.ResourceGraphService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for testing purpose.
 */
@Service
public class ResourceTestService {
  @Autowired
  private ResourceRepository resourceRepository;
  @Autowired
  private ResourceEdgeRepository edgeRepository;
  @Autowired
  private ResourceGraphService resourceGraphService;

  /**
   * Retrieves a resource by its unique identifier along with its associated edges up to a specified depth.
   */
  @Transactional(readOnly = true)
  public Resource getResourceById(String id, int edgesDepth) {
    return resourceRepository.findById(Long.parseLong(id))
      .map(resource -> {
        fetchEdges(resource, edgesDepth);
        return resource;
      })
      .orElseThrow(() -> new EntityNotFoundException("Resource not found by id: " + id));
  }

  private void fetchEdges(Resource resource, int edgesDepth) {
    if (edgesDepth <= 0) {
      return;
    }
    Stream.concat(resource.getIncomingEdges().stream(), resource.getOutgoingEdges().stream())
      .forEach(edge -> {
        fetchEdges(edge.getSource(), edgesDepth - 1);
        fetchEdges(edge.getTarget(), edgesDepth - 1);
      });
  }

  public Resource saveGraph(Resource resource) {
    return resourceGraphService.saveMergingGraph(resource);
  }

  public Optional<Resource> findById(long id) {
    return resourceRepository.findById(id);
  }

  public boolean existsById(Long id) {
    return resourceRepository.existsById(id);
  }

  public long countResources() {
    return resourceRepository.count();
  }

  public long countEdges() {
    return edgeRepository.count();
  }

  public Optional<ResourceEdge> findEdgeById(ResourceEdgePk id) {
    return edgeRepository.findById(id);
  }
}
