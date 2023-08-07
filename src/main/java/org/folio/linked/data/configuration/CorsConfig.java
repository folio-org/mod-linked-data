package org.folio.linked.data.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
@Profile("local")
public class CorsConfig {

  @Bean
  public CorsFilter corsFilter() {
    CorsConfiguration corsConfiguration = new CorsConfiguration();

    corsConfiguration.addAllowedOrigin("*"); //NOSONAR
    corsConfiguration.addAllowedMethod("*"); //NOSONAR
    corsConfiguration.addAllowedHeader("*"); //NOSONAR

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", corsConfiguration);

    return new CorsFilter(source);
  }
}
