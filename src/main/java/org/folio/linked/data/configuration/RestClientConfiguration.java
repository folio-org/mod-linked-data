package org.folio.linked.data.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfiguration {

  @Bean
  public RestClient restClient() {
    return RestClient.builder()
      .requestFactory(new JdkClientHttpRequestFactory())
      .build();
  }
}
