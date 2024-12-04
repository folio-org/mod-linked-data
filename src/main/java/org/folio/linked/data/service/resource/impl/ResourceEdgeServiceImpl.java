package org.folio.linked.data.service.resource.impl;

import static org.folio.ld.dictionary.PredicateDictionary.ADMIN_METADATA;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.service.resource.ResourceEdgeService;
import org.springframework.stereotype.Service;

@Service
public class ResourceEdgeServiceImpl implements ResourceEdgeService {

  private final Map<ResourceTypeDictionary, Set<PredicateDictionary>> edgesToBeCopied;

  public ResourceEdgeServiceImpl() {
    edgesToBeCopied = initializeEdgesToBeCopied();
  }

  public void copyOutgoingEdges(Resource from, Resource to) {
    getEdgesToBeCopied(from)
      .stream()
      .map(edge -> new ResourceEdge(to, edge.getTarget(), edge.getPredicate()))
      .forEach(to::addOutgoingEdge);
  }

  private Set<ResourceEdge> getEdgesToBeCopied(Resource resource) {
    var predicatesToBeCopied = getPredicatesToBeCopied(resource);
    return getOutgoingEdgesWithPredicate(resource, predicatesToBeCopied);
  }

  private static Set<ResourceEdge> getOutgoingEdgesWithPredicate(Resource resource, Set<String> predicateUris) {
    return resource.getOutgoingEdges().stream()
      .filter(edge -> predicateUris.contains(edge.getPredicate().getUri()))
      .collect(Collectors.toSet());
  }

  private Set<String> getPredicatesToBeCopied(Resource resource) {
    return edgesToBeCopied.entrySet().stream()
      .filter(entry -> resource.isOfType(entry.getKey()))
      .flatMap(entry -> entry.getValue().stream())
      .map(PredicateDictionary::getUri)
      .collect(Collectors.toSet());
  }

  private Map<ResourceTypeDictionary, Set<PredicateDictionary>> initializeEdgesToBeCopied() {
    return Map.of(
      ResourceTypeDictionary.INSTANCE, Set.of(ADMIN_METADATA)
    );
  }
}
