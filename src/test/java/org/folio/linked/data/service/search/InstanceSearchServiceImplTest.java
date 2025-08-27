package org.folio.linked.data.service.search;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import feign.FeignException;
import java.util.List;
import java.util.stream.Stream;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@UnitTest
@ExtendWith(MockitoExtension.class)
class InstanceSearchServiceImplTest {

  private static final String EXPECTED_QUERY =
    "(lccn==\"12\" or lccn==\"34\") and (staffSuppress <> \"true\" and discoverySuppress <> \"true\")";
  private static final String EXPECTED_QUERY_ID_EXCLUDED = EXPECTED_QUERY + " and id <> \"1234\"";

  @InjectMocks
  private InstanceSearchServiceImpl searchService;

  @Mock
  private SearchClient searchClient;

  @Test
  void shouldReturnZeroTotalRecords_ifSearchClientThrowsNotFound() {
    // given
    when(searchClient.searchInstances(any())).thenThrow(FeignException.NotFound.class);
    var lccn = List.of("");

    // expect
    assertThrows(FeignException.NotFound.class, () -> searchService.searchByLccn(lccn));
  }

  @Test
  void searchByLccn_shouldReturnResponseWithTotalRecords() {
    // given
    var lccns = List.of("12", "34");
    var expectedResponse = new ResponseEntity<>(new SearchResponseTotalOnly().totalRecords(1L), HttpStatus.OK);
    when(searchClient.searchInstances(EXPECTED_QUERY)).thenReturn(expectedResponse);

    // when
    var actualResponse = searchService.searchByLccn(lccns);

    // then
    assertEquals(expectedResponse.getBody(), actualResponse);
  }


  @ParameterizedTest
  @MethodSource("queryArgumentsProvider")
  void searchByLccnExcludingId_shouldReturnResponseWithTotalRecords(String inventoryId, String expectedQuery) {
    // given
    var lccns = List.of("12", "34");
    var expectedResponse = new ResponseEntity<>(new SearchResponseTotalOnly().totalRecords(1L), HttpStatus.OK);
    when(searchClient.searchInstances(expectedQuery)).thenReturn(expectedResponse);

    // when
    var actualResponse = searchService.searchByLccnExcludingId(lccns, inventoryId);

    // then
    assertEquals(expectedResponse.getBody(), actualResponse);
  }

  public static Stream<Arguments> queryArgumentsProvider() {
    return Stream.of(
      arguments("1234", EXPECTED_QUERY_ID_EXCLUDED),
      arguments(null, EXPECTED_QUERY)
    );
  }
}
