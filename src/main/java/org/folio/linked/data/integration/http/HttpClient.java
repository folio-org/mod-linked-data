package org.folio.linked.data.integration.http;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class HttpClient {

  private final RestClient restClient;

  public String downloadString(String url) {
    return restClient.get()
      .uri(url)
      .retrieve()
      .body(String.class);
  }
}
