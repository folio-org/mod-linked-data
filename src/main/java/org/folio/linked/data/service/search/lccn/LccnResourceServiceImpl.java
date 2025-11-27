package org.folio.linked.data.service.search.lccn;

import static java.lang.String.join;
import static java.util.Collections.emptyMap;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.folio.ld.dictionary.model.FolioMetadata;
import org.folio.ld.dictionary.model.Resource;
import org.folio.linked.data.domain.dto.AuthorityItem;
import org.folio.linked.data.domain.dto.AuthoritySearchResponse;
import org.folio.linked.data.integration.client.SearchClient;
import org.folio.linked.data.mapper.dto.ResourceSubgraphViewMapper;
import org.folio.linked.data.repo.ResourceSubgraphViewRepository;
import org.folio.linked.data.service.resource.marc.ResourceMarcAuthorityService;
import org.folio.rdf4ld.service.lccn.MockLccnResourceService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LccnResourceServiceImpl implements LccnResourceService {
  private static final String NOT_FOUND_MESSAGE = "Resource presented only with LCCN [%s] is not found in %s";
  private final SearchClient searchClient;
  private final MockLccnResourceService mockLccnResourceService;
  private final ResourceSubgraphViewMapper resourceSubgraphViewMapper;
  private final ResourceSubgraphViewRepository subgraphViewRepository;
  private final ResourceMarcAuthorityService resourceMarcAuthorityService;

  @Override
  public Map<String, LccnResourceSearchResult> findMockResources(Set<Resource> resources) {
    if (resources.isEmpty()) {
      return emptyMap();
    }
    var lccns = mockLccnResourceService.gatherLccns(resources);
    if (lccns.isEmpty()) {
      return emptyMap();
    }
    var searchQuery = "lccn any \"" + join(" ", lccns) + "\"";
    var lccnsToInventoryIds = ofNullable(searchClient.searchAuthorities(searchQuery)
      .getBody())
      .map(AuthoritySearchResponse::getAuthorities)
      .stream()
      .flatMap(Collection::stream)
      .filter(ai -> nonNull(ai.getId()))
      .collect(toMap(AuthorityItem::getNaturalId, AuthorityItem::getId));
    var foundSubgraphViews = subgraphViewRepository.findByInventoryIdIn(new HashSet<>(lccnsToInventoryIds.values()));
    return lccns.stream()
      .filter(lccnsToInventoryIds::containsKey)
      .collect(toMap(identity(), lccn -> {
        var inventoryId = lccnsToInventoryIds.get(lccn);
        var foundSgw = foundSubgraphViews.stream()
          .filter(sgw -> sgw.getInventoryId().equals(inventoryId))
          .findFirst();
        return new LccnResourceSearchResult(foundSgw.orElse(null), inventoryId);
      }));
  }

  @Override
  public Resource unMockLccnResource(Resource resourceModel, Map<String, LccnResourceSearchResult> searchResults) {
    return mockLccnResourceService.unMockLccnResource(resourceModel, lccn -> getLccnResource(lccn, searchResults));
  }

  private Resource getLccnResource(String lccn, Map<String, LccnResourceSearchResult> searchResults) {
    return ofNullable(searchResults.get(lccn))
      .map(searchResult -> {
        var inventoryId = searchResult.inventoryId();
        return ofNullable(searchResult.subgraphView())
          .flatMap(rsw ->
            resourceSubgraphViewMapper.fromJson(rsw.getResourceSubgraph())
              .map(r -> r.setFolioMetadata(new FolioMetadata().setInventoryId(rsw.getInventoryId())))
          )
          .or(() -> resourceMarcAuthorityService.fetchResourceFromSrsByInventoryId(inventoryId))
          .orElseThrow(
            () -> new RuntimeException(NOT_FOUND_MESSAGE.formatted(lccn, "SRS by inventoryId " + inventoryId))
          );
      })
      .orElseThrow(() -> new RuntimeException(NOT_FOUND_MESSAGE.formatted(lccn, "Search")));
  }
}
