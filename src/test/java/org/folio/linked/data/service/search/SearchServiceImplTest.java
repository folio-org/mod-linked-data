package org.folio.linked.data.service.search;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.OK;

import feign.FeignException;
import java.util.List;
import java.util.stream.Stream;
import org.folio.linked.data.domain.dto.AuthorityItem;
import org.folio.linked.data.domain.dto.AuthoritySearchResponse;
import org.folio.linked.data.domain.dto.SearchResponseTotalOnly;
import org.folio.linked.data.integration.client.SearchClient;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

@UnitTest
@ExtendWith(MockitoExtension.class)
class SearchServiceImplTest {

  private static final String EXPECTED_QUERY =
    "(lccn==\"12\" or lccn==\"34\") and (staffSuppress <> \"true\" and discoverySuppress <> \"true\")";
  private static final String EXPECTED_QUERY_ID_EXCLUDED = EXPECTED_QUERY + " and id <> \"1234\"";

  @InjectMocks
  private SearchServiceImpl searchService;

  @Mock
  private SearchClient searchClient;

  public static Stream<Arguments> queryArgumentsProvider() {
    return Stream.of(
      arguments("1234", EXPECTED_QUERY_ID_EXCLUDED),
      arguments(null, EXPECTED_QUERY)
    );
  }

  @Test
  void shouldReturnZeroTotalRecords_ifSearchClientThrowsNotFound() {
    // given
    when(searchClient.searchInstances(any())).thenThrow(FeignException.NotFound.class);
    var lccn = List.of("");

    // expect
    assertThrows(FeignException.NotFound.class, () ->
      searchService.getTotalInstancesByLccnExcludingSuppressedAndId(lccn, ""));
  }

  @Test
  void searchInstancesByLccn_ExcludingSuppressed_shouldReturnResponseWithTotalRecords() {
    // given
    var lccns = List.of("12", "34");
    var expectedResponse = new ResponseEntity<>(new SearchResponseTotalOnly().totalRecords(1L), OK);
    when(searchClient.searchInstances(EXPECTED_QUERY + " and id <> \"\"")).thenReturn(expectedResponse);

    // when
    var actualResponse = searchService.getTotalInstancesByLccnExcludingSuppressedAndId(lccns, "");

    // then
    assertThat(expectedResponse.getBody()).isEqualTo(actualResponse);
  }

  @ParameterizedTest
  @MethodSource("queryArgumentsProvider")
  void searchInstancesByLccnExcludingSuppressedExcludingId_shouldReturnResponseWithTotalRecords(String inventoryId,
                                                                                                String expectedQuery) {
    // given
    var lccns = List.of("12", "34");
    var expectedResponse = new ResponseEntity<>(new SearchResponseTotalOnly().totalRecords(1L), OK);
    when(searchClient.searchInstances(expectedQuery)).thenReturn(expectedResponse);

    // when
    var actualResponse = searchService.getTotalInstancesByLccnExcludingSuppressedAndId(lccns, inventoryId);

    // then
    assertThat(expectedResponse.getBody()).isEqualTo(actualResponse);
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
    assertThat(result).isEqualTo(expectedResponse);
  }
}
