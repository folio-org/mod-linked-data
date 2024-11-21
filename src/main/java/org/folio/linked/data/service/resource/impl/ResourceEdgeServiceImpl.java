package org.folio.linked.data.service.resource.impl;

import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@Service
public class ResourceEdgeServiceImpl {

  // If there are more conditions like this, move them to a separate class
  private static final Predicate<ResourceEdge> IS_ADMIN_METADATA = edge -> edge.getPredicate().getUri()
    .equals(PredicateDictionary.ADMIN_METADATA.getUri());

  private Map<ResourceTypeDictionary, List<Predicate<ResourceEdge>>> rules;

  public ResourceEdgeServiceImpl() {
    initializeRules();
  }

  public void copyOutgoingEdges(Resource from, Resource to) {
    this.rules.entrySet()
      .stream()
      .filter(entry -> to.isOfType(entry.getKey()))
      .map(Map.Entry::getValue)
      .flatMap(List::stream)
      .flatMap(condition -> from.getOutgoingEdges().stream().filter(condition))
      .map(edge -> new ResourceEdge(to, edge.getTarget(), edge.getPredicate()))
      .forEach( e -> to.addOutgoingEdge(new ResourceEdge(e)));
  }

  private void initializeRules() {
    this.rules = Map.of(
      ResourceTypeDictionary.WORK, List.of(IS_ADMIN_METADATA),
      ResourceTypeDictionary.INSTANCE, List.of(IS_ADMIN_METADATA)
    );
  }
}
