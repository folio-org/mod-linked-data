package org.folio.linked.data.service.resource.impl;

import java.util.Map;
import java.util.function.Predicate;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.service.resource.ResourceEdgeService;
import org.springframework.stereotype.Service;

@Service
public class ResourceEdgeServiceImpl implements ResourceEdgeService {

  private static final Predicate<ResourceEdge> IS_ADMIN_METADATA = edge -> edge.getPredicate().getUri()
    .equals(PredicateDictionary.ADMIN_METADATA.getUri());

  private final Map<ResourceTypeDictionary, Predicate<ResourceEdge>> edgesTobeCopied;

  public ResourceEdgeServiceImpl() {
    edgesTobeCopied = initializeEdgesTobeCopied();
  }

  public void copyOutgoingEdges(Resource from, Resource to) {
    this.edgesTobeCopied.entrySet()
      .stream()
      .filter(entry -> from.isOfType(entry.getKey()))
      .filter(entry -> to.isOfType(entry.getKey()))
      .map(Map.Entry::getValue)
      .flatMap(condition -> from.getOutgoingEdges().stream().filter(condition))
      .map(edge -> new ResourceEdge(to, edge.getTarget(), edge.getPredicate()))
      .forEach(to::addOutgoingEdge);
  }

  private Map<ResourceTypeDictionary, Predicate<ResourceEdge>> initializeEdgesTobeCopied() {
    return Map.of(
      ResourceTypeDictionary.WORK, IS_ADMIN_METADATA,
      ResourceTypeDictionary.INSTANCE, IS_ADMIN_METADATA
    );
  }
}
