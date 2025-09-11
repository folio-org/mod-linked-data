package org.folio.linked.data.service.resource.edge;

import static org.folio.ld.dictionary.PredicateDictionary.DISSERTATION;
import static org.folio.ld.dictionary.PredicateDictionary.GENRE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;

import java.util.Map;
import java.util.Set;
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
import org.folio.linked.data.repo.ResourceRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ResourceEdgeServiceImpl implements ResourceEdgeService {

  private static final Map<ResourceTypeDictionary, Set<PredicateDictionary>> EDGES_TO_BE_COPIED = Map.of(
    WORK, Set.of(DISSERTATION, GENRE)
  );
  private final ResourceRepository resourceRepository;
  private final ResourceModelMapper resourceModelMapper;
  private final ResourceEdgeRepository resourceEdgeRepository;

  @Override
  public void copyOutgoingEdges(Resource from, Resource to) {
    if (from.getTypes().equals(to.getTypes())) {
      getEdgesToBeCopied(from)
        .stream()
        .map(edge -> new ResourceEdge(to, edge.getTarget(), edge.getPredicate()))
        .forEach(to::addOutgoingEdge);
    }
  }

  @Override
  public long deleteEdgesHavingPredicate(Long resourceId, PredicateDictionary predicateToDelete) {
    return resourceEdgeRepository.deleteByIdSourceHashAndIdPredicateHash(resourceId, predicateToDelete.getHash());
  }

  @Override
  public ResourceEdgePk saveNewResourceEdge(Long sourceId,
                                            PredicateDictionary predicate,
                                            org.folio.ld.dictionary.model.Resource target) {
    var sourceRef = new Resource().setId(sourceId);
    var targetEntity = resourceModelMapper.toEntity(target);
    var savedTarget = resourceRepository.findById(targetEntity.getId())
      .orElse(resourceRepository.save(targetEntity));
    var edge = new ResourceEdge(sourceRef, savedTarget, new PredicateEntity(predicate));
    edge.computeId();
    return resourceEdgeRepository.save(edge).getId();
  }

  private Set<ResourceEdge> getEdgesToBeCopied(Resource resource) {
    var predicatesToBeCopied = getPredicatesToBeCopied(resource);
    return getOutgoingEdgesWithPredicate(resource, predicatesToBeCopied);
  }

  private Set<ResourceEdge> getOutgoingEdgesWithPredicate(Resource resource, Set<String> predicateUris) {
    return resource.getOutgoingEdges().stream()
      .filter(edge -> predicateUris.contains(edge.getPredicate().getUri()))
      .collect(Collectors.toSet());
  }

  private Set<String> getPredicatesToBeCopied(Resource resource) {
    return EDGES_TO_BE_COPIED.entrySet().stream()
      .filter(entry -> resource.isOfType(entry.getKey()))
      .flatMap(entry -> entry.getValue().stream())
      .map(PredicateDictionary::getUri)
      .collect(Collectors.toSet());
  }

}
