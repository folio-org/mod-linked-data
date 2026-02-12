package org.folio.linked.data.configuration;

import org.folio.ld.dictionary.label.LabelGeneratorService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LabelConfiguration {
  @Bean
  public LabelGeneratorService labelGeneratorService() {
    return new LabelGeneratorService();
  }
}
