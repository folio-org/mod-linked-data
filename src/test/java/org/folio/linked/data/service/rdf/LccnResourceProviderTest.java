package org.folio.linked.data.service.rdf;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.folio.linked.data.domain.dto.AuthorityItem;
import org.folio.linked.data.domain.dto.AuthoritySearchResponse;
import org.folio.linked.data.integration.client.SearchClient;
import org.folio.linked.data.mapper.ResourceModelMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.service.resource.marc.ResourceMarcAuthorityService;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

@UnitTest
@ExtendWith(MockitoExtension.class)
class LccnResourceProviderTest {

  @InjectMocks
  private LccnResourceProvider lccnResourceProvider;
  @Mock
  private SearchClient searchClient;
  @Mock
  private ResourceModelMapper resourceModelMapper;
  @Mock
  private ResourceMarcAuthorityService resourceMarcAuthorityService;

  @Test
  void shouldReturnFoundResource() {
    // given
    var lccn = "123456";
    var inventoryId = "inventoryId-123";
    var authorityItem = new AuthorityItem().id(inventoryId);
    var authoritiesResponse = new AuthoritySearchResponse().authorities(List.of(authorityItem));
    when(searchClient.searchAuthorities("lccn = " + lccn))
      .thenReturn(ResponseEntity.ok(authoritiesResponse));
    var fetchedResource = new Resource().setIdAndRefreshEdges(1L);
    when(resourceMarcAuthorityService.fetchAuthorityOrCreateByInventoryId(inventoryId))
      .thenReturn(Optional.of(fetchedResource));
    var expectedResource = new org.folio.ld.dictionary.model.Resource().setId(1L);
    when(resourceModelMapper.toModel(any(Resource.class)))
      .thenReturn(expectedResource);

    // when
    var result = lccnResourceProvider.apply(lccn);

    // then
    assertThat(result).isPresent().get().isEqualTo(expectedResource);
  }

  @Test
  void shouldReturnEmptyOptional_whenSearchReturnsNoResult() {
    // given
    var lccn = "123456";
    var authoritiesResponse = new AuthoritySearchResponse();
    when(searchClient.searchAuthorities("lccn = " + lccn))
      .thenReturn(ResponseEntity.ok(authoritiesResponse));

    // when
    var result = lccnResourceProvider.apply(lccn);

    // then
    assertThat(result).isEmpty();
  }

  @Test
  void shouldReturnEmptyOptional_whenSearchAndSrsReturnNoResult() {
    // given
    var lccn = "123456";
    var inventoryId = "inventoryId-123";
    var authorityItem = new AuthorityItem().id(inventoryId);
    var authoritiesResponse = new AuthoritySearchResponse().authorities(List.of(authorityItem));
    when(searchClient.searchAuthorities("lccn = " + lccn))
      .thenReturn(ResponseEntity.ok(authoritiesResponse));
    when(resourceMarcAuthorityService.fetchAuthorityOrCreateByInventoryId(inventoryId))
      .thenReturn(Optional.empty());

    // when
    var result = lccnResourceProvider.apply(lccn);

    // then
    assertThat(result).isEmpty();
  }

}
