package org.folio.linked.data.configuration.kafka;

import static org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG;
import static org.folio.linked.data.util.Constants.FOLIO_PROFILE;

import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.serialization.StringSerializer;
import org.folio.linked.data.integration.kafka.message.InstanceIngressEventMessage;
import org.folio.spring.tools.kafka.FolioMessageProducer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

@Configuration
@RequiredArgsConstructor
@Profile(FOLIO_PROFILE)
public class FolioKafkaConfig {
  private final KafkaProperties kafkaProperties;
  @Value("${mod-linked-data.kafka.topic.inventory.instance-ingress}")
  private String initialInventoryInstanceIngressTopic;

  @Bean
  public FolioMessageProducer<InstanceIngressEventMessage> instanceIngressMessageProducer(
    KafkaTemplate<String, InstanceIngressEventMessage> instanceIngressMessageTemplate
  ) {
    var producer = new FolioMessageProducer<>(instanceIngressMessageTemplate,
      () -> initialInventoryInstanceIngressTopic);
    producer.setKeyMapper(InstanceIngressEventMessage::getId);
    return producer;
  }

  @Bean
  public KafkaTemplate<String, InstanceIngressEventMessage> instanceIngressMessageTemplate(
    ProducerFactory<String, InstanceIngressEventMessage> instanceIngressMessageProducerFactory
  ) {
    return new KafkaTemplate<>(instanceIngressMessageProducerFactory);
  }

  @Bean
  public ProducerFactory<String, InstanceIngressEventMessage> instanceIngressEventMessageProducerFactory() {
    Map<String, Object> configProps = new HashMap<>(kafkaProperties.buildProducerProperties(null));
    configProps.put(KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    configProps.put(VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
    return new DefaultKafkaProducerFactory<>(configProps);
  }

}
