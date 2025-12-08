package org.folio.linked.data.configuration.kafka;

import static org.folio.linked.data.util.Constants.STANDALONE_PROFILE;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Data
@Component
@Profile("!" + STANDALONE_PROFILE)
@ConfigurationProperties("mod-linked-data.kafka.topic")
public class LinkedDataTopicProperties {

  private String workSearchIndex;
  private String hubSearchIndex;
  private String instanceIngress;
  private String linkedDataImportResult;
}
