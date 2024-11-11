package org.folio.linked.data.test.kafka;

import static org.folio.linked.data.util.Constants.STANDALONE_PROFILE;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.configuration.kafka.LinkedDataTopicProperties;
import org.folio.linked.data.domain.dto.InstanceIngressEvent;
import org.folio.spring.tools.kafka.FolioMessageProducer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;

@TestConfiguration
@RequiredArgsConstructor
@Profile("!" + STANDALONE_PROFILE)
public class KafkaProducerTestConfiguration {

  private final LinkedDataTopicProperties linkedDataTopicProperties;

  @Bean
  @Primary
  public FolioMessageProducer<InstanceIngressEvent> instanceIngressEventProducer(
    KafkaTemplate<String, InstanceIngressEvent> instanceIngressMessageTemplate
  ) {
    var producer = new FolioMessageProducer<>(instanceIngressMessageTemplate,
      linkedDataTopicProperties::getInstanceIngress);
    producer.setKeyMapper(iie -> iie.getEventPayload().getSourceRecordIdentifier());
    return producer;
  }
}
