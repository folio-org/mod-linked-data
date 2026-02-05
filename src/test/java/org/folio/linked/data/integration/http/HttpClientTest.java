package org.folio.linked.data.integration.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

@UnitTest
@ExtendWith(MockitoExtension.class)
class HttpClientTest {

  @InjectMocks
  private HttpClient httpClient;

  @Mock
  private RestClient restClient;

  @Test
  void shouldDownloadStringFromValidUrl() {
    // given
    var url = "https://example.com/test.json";
    var expectedJson = "{\"test\":\"data\"}";
    var requestHeadersUriSpec = mockRestClientChain(expectedJson);
    when(restClient.get()).thenReturn(requestHeadersUriSpec);

    // when
    var result = httpClient.downloadString(url);

    // then
    assertEquals(expectedJson, result);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private RestClient.RequestHeadersUriSpec mockRestClientChain(String expectedJson) {
    var requestHeadersUriSpec = mock(RestClient.RequestHeadersUriSpec.class);
    var responseSpec = mock(RestClient.ResponseSpec.class);
    when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.body(String.class)).thenReturn(expectedJson);
    return requestHeadersUriSpec;
  }

}
