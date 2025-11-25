package org.folio.linked.data.service.search.lccn;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.folio.linked.data.service.search.lccn.LccnResourceService.LccnResourceSearchResult;
import static org.folio.linked.data.test.TestUtil.getLccnResourceSearchResult;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.OK;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import org.folio.ld.dictionary.model.Resource;
import org.folio.linked.data.domain.dto.AuthorityItem;
import org.folio.linked.data.domain.dto.AuthoritySearchResponse;
import org.folio.linked.data.integration.client.SearchClient;
import org.folio.linked.data.mapper.dto.ResourceSubgraphViewMapper;
import org.folio.linked.data.model.entity.ResourceSubgraphView;
import org.folio.linked.data.repo.ResourceSubgraphViewRepository;
import org.folio.linked.data.service.resource.marc.ResourceMarcAuthorityService;
import org.folio.rdf4ld.service.lccn.MockLccnResourceService;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

@UnitTest
@ExtendWith(MockitoExtension.class)
class LccnResourceServiceTest {

  @InjectMocks
  private LccnResourceServiceImpl lccnResourceSearchService;
  @Mock
  private SearchClient searchClient;
  @Mock
  private MockLccnResourceService mockLccnResourceService;
  @Mock
  private ResourceSubgraphViewMapper resourceSubgraphViewMapper;
  @Mock
  private ResourceSubgraphViewRepository subgraphViewRepository;
  @Mock
  private ResourceMarcAuthorityService resourceMarcAuthorityService;

  @Test
  void shouldReturnResultsWithFoundResourceAndInventoryId() {
    // given
    var lccn1 = "lccnOfExistedResource";
    var lccn2 = "lccnOfNotExistedResourceButFoundInventoryId";
    var lccn3 = "lccnOfNotExistedAndNotFoundInventoryIdResource";
    var lccns = new LinkedHashSet<>(List.of(lccn1, lccn2, lccn3));
    var searchQuery = "lccn any \"" + lccn1 + " " + lccn2 + " " + lccn3 + "\"";
    var invId1 = "inventoryId1";
    var inventoryId2 = "inventoryId2";
    var searchResponse = new ResponseEntity<>(new AuthoritySearchResponse().authorities(List.of(
      new AuthorityItem().id(invId1).naturalId(lccn1),
      new AuthorityItem().id(inventoryId2).naturalId(lccn2)
    )), OK);
    doReturn(searchResponse).when(searchClient).searchAuthorities(searchQuery);
    var foundSubgraph = new ResourceSubgraphView()
      .setInventoryId(invId1)
      .setResourceSubgraph("lccnOfExistedResource subgraph");
    doReturn(Set.of(foundSubgraph)).when(subgraphViewRepository).findByInventoryIdIn(Set.of(invId1, inventoryId2));
    var resources = Set.of(new Resource().setId(1L));
    doReturn(lccns).when(mockLccnResourceService).gatherLccns(resources);

    // when
    var result = lccnResourceSearchService.findMockResources(resources);

    // then
    assertThat(result).hasSize(2);
    assertThat(result.get(lccn1)).isEqualTo(getLccnResourceSearchResult(foundSubgraph.getResourceSubgraph(), invId1));
    assertThat(result.get(lccn2)).isEqualTo(new LccnResourceSearchResult(null, inventoryId2));
    assertThat(result.get(lccn3)).isNull();
  }

  @Test
  void shouldReturnNothing_ifNoGivenResources() {

    // when
    var result = lccnResourceSearchService.findMockResources(new HashSet<>());

    // then
    assertThat(result).isEmpty();
  }

  @Test
  void shouldReturnNothing_ifNoMockLccnResources() {
    // given
    var resources = Set.of(new Resource().setId(1L));
    var lccns = Set.of();
    doReturn(lccns).when(mockLccnResourceService).gatherLccns(resources);

    // when
    var result = lccnResourceSearchService.findMockResources(resources);

    // then
    assertThat(result).isEmpty();
  }

  @Test
  void unMockLccnResource_shouldReturnMappedResourceFromSearchResults_ifPresentedInGraph() {
    // given
    var lccn = UUID.randomUUID().toString();
    var searchResults = new HashMap<String, LccnResourceSearchResult>();
    searchResults.put(lccn, getLccnResourceSearchResult("1", null));
    searchResults.put(lccn + "2", getLccnResourceSearchResult("2", null));
    searchResults.put(lccn + "3", getLccnResourceSearchResult("3", null));
    var resource = new org.folio.ld.dictionary.model.Resource().setId(1L);
    doReturn(of(resource)).when(resourceSubgraphViewMapper).fromJson("1");
    var mockResource = new Resource().setId(2L);
    when(mockLccnResourceService.unMockLccnResource(any(), any())).thenAnswer(inv -> {
      var lccnProvider = (Function<String, Resource>) inv.getArgument(1);
      return lccnProvider.apply(lccn);
    });

    // when
    var result = lccnResourceSearchService.unMockLccnResource(mockResource, searchResults);

    // then
    assertThat(result).isEqualTo(resource);
  }

  @Test
  void unMockLccnResource_shouldReturnResourceFromResourceMarcAuthorityService_ifNotPresentedInGraph() {
    // given
    var lccn = UUID.randomUUID().toString();
    var searchResults = new HashMap<String, LccnResourceSearchResult>();
    var inventoryId = UUID.randomUUID().toString();
    searchResults.put(lccn, new LccnResourceSearchResult(null, inventoryId));
    searchResults.put(lccn + "2", getLccnResourceSearchResult("2", null));
    searchResults.put(lccn + "3", getLccnResourceSearchResult("3", null));
    var resource = new org.folio.ld.dictionary.model.Resource().setId(1L);
    doReturn(of(resource)).when(resourceMarcAuthorityService).fetchResourceFromSrsByInventoryId(inventoryId);
    var mockResource = new Resource().setId(2L);
    when(mockLccnResourceService.unMockLccnResource(any(), any())).thenAnswer(inv -> {
      var lccnProvider = (Function<String, Resource>) inv.getArgument(1);
      return lccnProvider.apply(lccn);
    });

    // when
    var result = lccnResourceSearchService.unMockLccnResource(mockResource, searchResults);

    // then
    assertThat(result).isEqualTo(resource);
  }

  @Test
  void unMockLccnResource_shouldThrowException_ifResourceNotFoundByResourceMarcAuthorityService() {
    // given
    var lccn = UUID.randomUUID().toString();
    var searchResults = new HashMap<String, LccnResourceSearchResult>();
    var inventoryId = UUID.randomUUID().toString();
    searchResults.put(lccn, new LccnResourceSearchResult(null, inventoryId));
    searchResults.put(lccn + "2", getLccnResourceSearchResult("2", null));
    searchResults.put(lccn + "3", getLccnResourceSearchResult("3", null));
    doReturn(empty()).when(resourceMarcAuthorityService).fetchResourceFromSrsByInventoryId(inventoryId);
    var mockResource = new Resource().setId(2L);
    when(mockLccnResourceService.unMockLccnResource(any(), any())).thenAnswer(inv -> {
      var lccnProvider = (Function<String, Resource>) inv.getArgument(1);
      return lccnProvider.apply(lccn);
    });

    // when
    var result = assertThatThrownBy(() -> lccnResourceSearchService.unMockLccnResource(mockResource, searchResults));

    // then
    result.hasMessage("Resource presented only with LCCN [" + lccn
      + "] is not found in SRS by inventoryId " + inventoryId);
  }

  @Test
  void unMockLccnResource_shouldThrowException_ifResourceNotFoundBySearch() {
    // given
    var lccn = UUID.randomUUID().toString();
    var searchResults = new HashMap<String, LccnResourceSearchResult>();
    searchResults.put(lccn + "2", getLccnResourceSearchResult("2", null));
    searchResults.put(lccn + "3", getLccnResourceSearchResult("3", null));
    var mockResource = new Resource().setId(2L);
    when(mockLccnResourceService.unMockLccnResource(any(), any())).thenAnswer(inv -> {
      var lccnProvider = (Function<String, Resource>) inv.getArgument(1);
      return lccnProvider.apply(lccn);
    });

    // when
    var result = assertThatThrownBy(() -> lccnResourceSearchService.unMockLccnResource(mockResource, searchResults));

    // then
    result.hasMessage("Resource presented only with LCCN [" + lccn + "] is not found in Search");
  }
}
