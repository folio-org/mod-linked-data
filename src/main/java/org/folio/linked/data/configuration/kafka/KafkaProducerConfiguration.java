package org.folio.linked.data.configuration.kafka;

import static org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG;
import static org.folio.linked.data.util.Constants.FOLIO_PROFILE;

import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.serialization.StringSerializer;
import org.folio.search.domain.dto.InstanceIngressEvent;
import org.folio.search.domain.dto.SearchIndexEvent;
import org.folio.spring.tools.kafka.FolioMessageProducer;
import org.jetbrains.annotations.NotNull;
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
public class KafkaProducerConfiguration {
  private final KafkaProperties kafkaProperties;
  @Value("${mod-linked-data.kafka.topic.search.bibframe-index}")
  private String initialBibframeIndexTopic;
  @Value("${mod-linked-data.kafka.topic.inventory.instance-ingress}")
  private String initialInventoryInstanceIngressTopic;

  @Bean
  public FolioMessageProducer<SearchIndexEvent> searchIndexEventProducer(
    KafkaTemplate<String, SearchIndexEvent> searchIndexEventMessageTemplate
  ) {
    var producer = new FolioMessageProducer<>(searchIndexEventMessageTemplate, () -> initialBibframeIndexTopic);
    producer.setKeyMapper(SearchIndexEvent::getId);
    return producer;
  }

  @Bean
  public FolioMessageProducer<InstanceIngressEvent> instanceIngressEventProducer(
    KafkaTemplate<String, InstanceIngressEvent> instanceIngressMessageTemplate
  ) {
    var producer = new FolioMessageProducer<>(instanceIngressMessageTemplate,
      () -> initialInventoryInstanceIngressTopic);
    producer.setKeyMapper(InstanceIngressEvent::getId);
    return producer;
  }

  @Bean
  public KafkaTemplate<String, SearchIndexEvent> searchIndexEventTemplate(
    ProducerFactory<String, SearchIndexEvent> searchIndexEventMessageProducerFactory) {
    return new KafkaTemplate<>(searchIndexEventMessageProducerFactory);
  }

  @Bean
  public KafkaTemplate<String, InstanceIngressEvent> instanceIngressEventTemplate(
    ProducerFactory<String, InstanceIngressEvent> instanceIngressMessageProducerFactory
  ) {
    return new KafkaTemplate<>(instanceIngressMessageProducerFactory);
  }

  @Bean
  public ProducerFactory<String, SearchIndexEvent> searchIndexEventProducerFactory() {
    return new DefaultKafkaProducerFactory<>(getProducerProperties());
  }

  @Bean
  public ProducerFactory<String, InstanceIngressEvent> instanceIngressEventProducerFactory() {
    return new DefaultKafkaProducerFactory<>(getProducerProperties());
  }

  private @NotNull Map<String, Object> getProducerProperties() {
    Map<String, Object> configProps = new HashMap<>(kafkaProperties.buildProducerProperties(null));
    configProps.put(KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    configProps.put(VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
    return configProps;
  }

}
