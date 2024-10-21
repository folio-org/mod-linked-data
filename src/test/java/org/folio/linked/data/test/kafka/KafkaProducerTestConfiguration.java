package org.folio.linked.data.test.kafka;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.configuration.kafka.LinkedDataTopicProperties;
import org.folio.linked.data.domain.dto.InstanceIngressEvent;
import org.folio.spring.tools.kafka.FolioMessageProducer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.KafkaTemplate;

@TestConfiguration
@RequiredArgsConstructor
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
