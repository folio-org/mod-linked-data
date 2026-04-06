package org.folio.linked.data.service.resource.edge;

import static org.folio.ld.dictionary.PredicateDictionary.DISSERTATION;
import static org.folio.ld.dictionary.PredicateDictionary.GENRE;
import static org.folio.ld.dictionary.PredicateDictionary.IS_PART_OF;
import static org.folio.ld.dictionary.PredicateDictionary.OTHER_EDITION;
import static org.folio.ld.dictionary.PredicateDictionary.OTHER_VERSION;
import static org.folio.ld.dictionary.PredicateDictionary.RELATED_WORK;
import static org.folio.ld.dictionary.ResourceTypeDictionary.SERIES;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;

import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.mapper.ResourceModelMapper;
import org.folio.linked.data.model.entity.PredicateEntity;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.model.entity.pk.ResourceEdgePk;
import org.folio.linked.data.repo.ResourceEdgeRepository;
import org.folio.linked.data.service.resource.graph.ResourceGraphService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ResourceEdgeServiceImpl implements ResourceEdgeService {

  private static final Map<ResourceTypeDictionary, Map<PredicateDictionary, Predicate<ResourceEdge>>>
    PARENT_TO_OUTGOING_EDGE_AND_CONDITION = Map.of(
    WORK, Map.of(
      DISSERTATION, edge -> true,
      GENRE, edge -> true,
      IS_PART_OF, edge -> !edge.getTarget().isOfType(SERIES),
      OTHER_EDITION, edge -> true,
      OTHER_VERSION, edge -> true,
      RELATED_WORK, edge -> true
    )
  );
  private final ResourceModelMapper resourceModelMapper;
  private final ResourceEdgeRepository resourceEdgeRepository;
  private final ResourceGraphService resourceGraphService;

  @Override
  public void copyOutgoingEdges(Resource from, Resource to) {
    if (from.getTypes().equals(to.getTypes())) {
      getOutgoingEdgesToBeCopied(from)
        .stream()
        .map(edge -> new ResourceEdge(to, edge.getTarget(), edge.getPredicate()))
        .forEach(to::addOutgoingEdge);
    }
  }

  @Override
  public void copyIncomingEdges(Resource from, Resource to) {
    if (from.getTypes().equals(to.getTypes())) {
      from.getIncomingEdges()
        .stream()
        .map(edge -> new ResourceEdge(edge.getSource(), to, edge.getPredicate()))
        .forEach(to::addIncomingEdge);
    }
  }

  @Override
  public Long deleteEdgesHavingPredicate(Long resourceId, PredicateDictionary predicateToDelete) {
    return resourceEdgeRepository.deleteByIdSourceHashAndIdPredicateHash(resourceId, predicateToDelete.getHash());
  }

  @Override
  public ResourceEdgePk saveNewResourceEdge(Long sourceId,
                                            PredicateDictionary predicate,
                                            org.folio.ld.dictionary.model.Resource targetModel) {
    var sourceRef = new Resource().setIdAndRefreshEdges(sourceId);
    var target = resourceModelMapper.toEntity(targetModel);
    var saveResult = resourceGraphService.saveMergingGraph(target);
    var edge = new ResourceEdge(sourceRef, saveResult.rootResource(), new PredicateEntity(predicate));
    edge.computeId();
    return resourceEdgeRepository.save(edge).getId();
  }

  private Set<ResourceEdge> getOutgoingEdgesToBeCopied(Resource resource) {
    return PARENT_TO_OUTGOING_EDGE_AND_CONDITION.entrySet().stream()
      .filter(entry -> resource.isOfType(entry.getKey()))
      .flatMap(entry -> resource.getOutgoingEdges().stream()
        .filter(edge -> PredicateDictionary.fromUri(edge.getPredicate().getUri())
          .map(entry.getValue()::get)
          .map(condition -> condition.test(edge))
          .orElse(false)))
      .collect(Collectors.toSet());
  }

}
