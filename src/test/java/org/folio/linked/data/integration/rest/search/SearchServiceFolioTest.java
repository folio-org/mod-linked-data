package org.folio.linked.data.integration.rest.search;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.OK;

import java.util.List;
import java.util.stream.Stream;
import org.folio.linked.data.domain.dto.AuthorityItem;
import org.folio.linked.data.domain.dto.AuthoritySearchResponse;
import org.folio.linked.data.domain.dto.SearchResponseTotalOnly;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;

@UnitTest
@ExtendWith(MockitoExtension.class)
class SearchServiceFolioTest {

  private static final String EXPECTED_QUERY =
    "(lccn==\"12\" or lccn==\"34\") and (staffSuppress <> \"true\" and discoverySuppress <> \"true\")";
  private static final String EXPECTED_QUERY_ID_EXCLUDED = EXPECTED_QUERY + " and id <> \"1234\"";

  @InjectMocks
  private SearchServiceFolio searchService;

  @Mock
  private SearchClient searchClient;

  public static Stream<Arguments> queryArgumentsProvider() {
    return Stream.of(
      arguments("1234", EXPECTED_QUERY_ID_EXCLUDED),
      arguments(null, EXPECTED_QUERY)
    );
  }

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(searchService, "searchMaxParams", 100);
  }

  @Test
  void shouldReturnZeroTotalRecords_ifSearchClientThrowsNotFound() {
    // given
    when(searchClient.searchInstances(any())).thenThrow(HttpClientErrorException.NotFound.class);
    var lccn = List.of("");

    // expect
    assertThrows(HttpClientErrorException.NotFound.class, () ->
      searchService.countInstancesByLccnExcludingSuppressedAndId(lccn, ""));
  }

  @Test
  void searchInstancesByLccn_ExcludingSuppressed_shouldReturnTotalRecords() {
    // given
    var lccns = List.of("12", "34");
    var expectedResponse = new ResponseEntity<>(new SearchResponseTotalOnly().totalRecords(1L), OK);
    when(searchClient.searchInstances(EXPECTED_QUERY + " and id <> \"\"")).thenReturn(expectedResponse);

    // when
    var actualResponse = searchService.countInstancesByLccnExcludingSuppressedAndId(lccns, "");

    // then
    assertThat(actualResponse).isEqualTo(1L);
  }

  @ParameterizedTest
  @MethodSource("queryArgumentsProvider")
  void searchInstancesByLccnExcludingSuppressedExcludingId_shouldReturnTotalRecords(String inventoryId,
                                                                                    String expectedQuery) {
    // given
    var lccns = List.of("12", "34");
    var expectedResponse = new ResponseEntity<>(new SearchResponseTotalOnly().totalRecords(1L), OK);
    when(searchClient.searchInstances(expectedQuery)).thenReturn(expectedResponse);

    // when
    var actualResponse = searchService.countInstancesByLccnExcludingSuppressedAndId(lccns, inventoryId);

    // then
    assertThat(actualResponse).isEqualTo(1L);
  }

  @Test
  void getAuthoritiesByLccn_shouldResponse() {
    // given
    var lccns = List.of("12", "34");
    var expectedResponse = new AuthoritySearchResponse().authorities(List.of(new AuthorityItem().naturalId("lccn1")));
    when(searchClient.searchAuthorities("(lccn==\"12\" or lccn==\"34\") and (authRefType==(\"Authorized\"))"))
      .thenReturn(new ResponseEntity<>(expectedResponse, OK));

    // when
    var result = searchService.getAuthoritiesByLccn(lccns);

    // then
    assertThat(result).isEqualTo(expectedResponse.getAuthorities());
  }

  @Test
  void getAuthoritiesByLccn_shouldProcessInChunks() {
    // given
    ReflectionTestUtils.setField(searchService, "searchMaxParams", 1);
    var lccns = List.of("12", "34", "56");
    var response1 = new AuthoritySearchResponse().authorities(List.of(new AuthorityItem().naturalId("lccn1")));
    var response2 = new AuthoritySearchResponse().authorities(List.of(new AuthorityItem().naturalId("lccn2")));
    var response3 = new AuthoritySearchResponse().authorities(List.of(new AuthorityItem().naturalId("lccn3")));

    when(searchClient.searchAuthorities("(lccn==\"12\") and (authRefType==(\"Authorized\"))"))
      .thenReturn(new ResponseEntity<>(response1, OK));
    when(searchClient.searchAuthorities("(lccn==\"34\") and (authRefType==(\"Authorized\"))"))
      .thenReturn(new ResponseEntity<>(response2, OK));
    when(searchClient.searchAuthorities("(lccn==\"56\") and (authRefType==(\"Authorized\"))"))
      .thenReturn(new ResponseEntity<>(response3, OK));

    // when
    var result = searchService.getAuthoritiesByLccn(lccns);

    // then
    assertThat(result).hasSize(3);
    assertThat(result.get(0).getNaturalId()).isEqualTo("lccn1");
    assertThat(result.get(1).getNaturalId()).isEqualTo("lccn2");
    assertThat(result.get(2).getNaturalId()).isEqualTo("lccn3");
  }

  @Test
  void countInstancesByLccnExcludingSuppressedAndId_shouldProcessInChunks() {
    // given
    ReflectionTestUtils.setField(searchService, "searchMaxParams", 1);
    var lccns = List.of("12", "34", "56");
    var query1 = "(lccn==\"12\") and (staffSuppress <> \"true\" and discoverySuppress <> \"true\")";
    var query2 = "(lccn==\"34\") and (staffSuppress <> \"true\" and discoverySuppress <> \"true\")";
    var query3 = "(lccn==\"56\") and (staffSuppress <> \"true\" and discoverySuppress <> \"true\")";

    when(searchClient.searchInstances(query1))
      .thenReturn(new ResponseEntity<>(new SearchResponseTotalOnly().totalRecords(2L), OK));
    when(searchClient.searchInstances(query2))
      .thenReturn(new ResponseEntity<>(new SearchResponseTotalOnly().totalRecords(3L), OK));
    when(searchClient.searchInstances(query3))
      .thenReturn(new ResponseEntity<>(new SearchResponseTotalOnly().totalRecords(5L), OK));

    // when
    var result = searchService.countInstancesByLccnExcludingSuppressedAndId(lccns, null);

    // then
    assertThat(result).isEqualTo(10L);
  }

  @Test
  void countInstancesByLccnExcludingSuppressedAndId_shouldProcessInChunksWithExcludeId() {
    // given
    ReflectionTestUtils.setField(searchService, "searchMaxParams", 1);
    var lccns = List.of("12", "34");
    var excludeId = "test-id";
    var q1 = "(lccn==\"12\") and (staffSuppress <> \"true\" and discoverySuppress <> \"true\") and id <> \"test-id\"";
    var q2 = "(lccn==\"34\") and (staffSuppress <> \"true\" and discoverySuppress <> \"true\") and id <> \"test-id\"";

    when(searchClient.searchInstances(q1))
      .thenReturn(new ResponseEntity<>(new SearchResponseTotalOnly().totalRecords(1L), OK));
    when(searchClient.searchInstances(q2))
      .thenReturn(new ResponseEntity<>(new SearchResponseTotalOnly().totalRecords(2L), OK));

    // when
    var result = searchService.countInstancesByLccnExcludingSuppressedAndId(lccns, excludeId);

    // then
    assertThat(result).isEqualTo(3L);
  }
}
