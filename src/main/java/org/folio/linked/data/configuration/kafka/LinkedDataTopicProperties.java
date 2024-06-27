package org.folio.linked.data.configuration.kafka;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("mod-linked-data.kafka.topic")
public class LinkedDataTopicProperties {

  private String searchBibframeIndex;
  private String searchBibframeAuthoritiesIndex;
  private String inventoryInstanceIngress;
}
