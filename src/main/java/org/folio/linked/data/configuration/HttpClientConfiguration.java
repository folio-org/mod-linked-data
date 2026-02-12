package org.folio.linked.data.configuration;

import org.folio.linked.data.integration.rest.authoritysource.AuthoritySourceFilesClient;
import org.folio.linked.data.integration.rest.configuration.ConfigurationClient;
import org.folio.linked.data.integration.rest.search.SearchClient;
import org.folio.linked.data.integration.rest.settings.SettingsClient;
import org.folio.linked.data.integration.rest.specification.SpecClient;
import org.folio.linked.data.integration.rest.srs.SrsClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class HttpClientConfiguration {

  @Bean
  public AuthoritySourceFilesClient authoritySourceFilesClient(HttpServiceProxyFactory factory) {
    return factory.createClient(AuthoritySourceFilesClient.class);
  }

  @Bean
  public ConfigurationClient configurationClient(HttpServiceProxyFactory factory) {
    return factory.createClient(ConfigurationClient.class);
  }

  @Bean
  public SearchClient searchClient(HttpServiceProxyFactory factory) {
    return factory.createClient(SearchClient.class);
  }

  @Bean
  public SettingsClient settingsClient(HttpServiceProxyFactory factory) {
    return factory.createClient(SettingsClient.class);
  }

  @Bean
  public SpecClient specClient(HttpServiceProxyFactory factory) {
    return factory.createClient(SpecClient.class);
  }

  @Bean
  public SrsClient srsClient(HttpServiceProxyFactory factory) {
    return factory.createClient(SrsClient.class);
  }
}
