package org.folio.linked.data.service.lccn;

import static java.util.Collections.emptyMap;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.folio.linked.data.util.Constants.STANDALONE_PROFILE;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.ld.dictionary.model.FolioMetadata;
import org.folio.ld.dictionary.model.Resource;
import org.folio.linked.data.domain.dto.AuthorityItem;
import org.folio.linked.data.integration.rest.search.SearchService;
import org.folio.linked.data.mapper.dto.ResourceSubgraphViewMapper;
import org.folio.linked.data.repo.ResourceSubgraphViewRepository;
import org.folio.linked.data.service.resource.marc.ResourceMarcAuthorityService;
import org.folio.rdf4ld.service.lccn.MockLccnResourceService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
@Profile("!" + STANDALONE_PROFILE)
public class LccnResourceServiceFolio implements LccnResourceService {
  private final SearchService searchService;
  private final MockLccnResourceService mockLccnResourceService;
  private final ResourceSubgraphViewMapper resourceSubgraphViewMapper;
  private final ResourceSubgraphViewRepository subgraphViewRepository;
  private final ResourceMarcAuthorityService resourceMarcAuthorityService;

  @Override
  public Map<String, LccnResourceSearchResult> findMockResources(Set<Resource> resources) {
    log.info("Finding mock resources: {}", resources);
    if (resources.isEmpty()) {
      return emptyMap();
    }
    var lccns = mockLccnResourceService.gatherLccns(resources);
    log.info("Finding mock resources: lccns: {}", lccns);
    if (lccns.isEmpty()) {
      return emptyMap();
    }
    var lccnsToInventoryIds = searchService.getAuthoritiesByLccn(lccns)
      .stream()
      .filter(ai -> nonNull(ai.getId()) && nonNull(ai.getNaturalId()))
      .collect(groupingBy(AuthorityItem::getNaturalId, mapping(AuthorityItem::getId, toSet())));
    log.info("Finding mock resources: lccnsToInventoryIds: {}", lccnsToInventoryIds);
    var allInventoryIds = lccnsToInventoryIds.values().stream()
      .flatMap(Set::stream)
      .collect(toSet());
    log.info("Finding mock resources: allInventoryIds: {}", allInventoryIds);
    var foundSubgraphViews = subgraphViewRepository.findByInventoryIdIn(allInventoryIds);

    var result = lccns.stream()
      .filter(lccnsToInventoryIds::containsKey)
      .collect(toMap(identity(), lccn -> {
        var inventoryIds = lccnsToInventoryIds.get(lccn);
        return foundSubgraphViews.stream()
          .filter(sgw -> inventoryIds.contains(sgw.getInventoryId()))
          .findFirst()
          .map(foundSgw -> new LccnResourceSearchResult(foundSgw, foundSgw.getInventoryId()))
          .orElseGet(() -> new LccnResourceSearchResult(null, inventoryIds.iterator().next()));
      }));
    log.info("Finding mock resources: result: {}", result);
    return result;
  }

  @Override
  public Resource unMockLccnEdges(Resource resource, Map<String, LccnResourceSearchResult> searchResults) {
    log.info("unMockLccnEdges: {}", resource);
    var unMocked = mockLccnResourceService.unMockLccnEdges(resource, lccn -> getLccnResource(lccn, searchResults));
    log.info("unMockLccnEdges: result: {}", unMocked);
    return unMocked;
  }

  private Optional<Resource> getLccnResource(String lccn, Map<String, LccnResourceSearchResult> searchResults) {
    return ofNullable(searchResults.get(lccn))
      .flatMap(searchResult -> {
        var inventoryId = searchResult.inventoryId();
        return ofNullable(searchResult.subgraphView())
          .flatMap(rsw ->
            resourceSubgraphViewMapper.fromJson(rsw.getResourceSubgraph())
              .map(r -> r.setFolioMetadata(new FolioMetadata().setInventoryId(rsw.getInventoryId())))
          )
          .or(() -> resourceMarcAuthorityService.fetchResourceFromSrsByInventoryId(inventoryId));
      });
  }
}
