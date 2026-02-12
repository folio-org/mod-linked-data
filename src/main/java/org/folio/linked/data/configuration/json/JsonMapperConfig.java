package org.folio.linked.data.configuration.json;

import static org.folio.linked.data.util.JsonUtils.JSON_MAPPER;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class JsonMapperConfig {

  @Bean
  @Primary
  public JsonMapper jacksonJsonMapper() {
    return JSON_MAPPER;
  }
}



