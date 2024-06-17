package org.folio.linked.data.configuration.kafka;

import static org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG;
import static org.folio.linked.data.util.Constants.FOLIO_PROFILE;
import static org.folio.linked.data.util.Constants.SEARCH_PROFILE;

import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.serialization.StringSerializer;
import org.folio.linked.data.integration.kafka.message.SearchIndexEventMessage;
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
@Profile({SEARCH_PROFILE, FOLIO_PROFILE})
public class SearchKafkaConfig {
  private final KafkaProperties kafkaProperties;
  @Value("${mod-linked-data.kafka.topic.search.bibframe-index}")
  private String initialBibframeIndexTopic;

  @Bean
  public FolioMessageProducer<SearchIndexEventMessage> searchIndexEventMessageProducer(
    KafkaTemplate<String, SearchIndexEventMessage> searchIndexEventMessageTemplate
  ) {
    var producer = new FolioMessageProducer<>(searchIndexEventMessageTemplate, () -> initialBibframeIndexTopic);
    producer.setKeyMapper(SearchIndexEventMessage::getId);
    return producer;
  }

  @Bean
  public KafkaTemplate<String, SearchIndexEventMessage> searchIndexEventMessageTemplate(
    ProducerFactory<String, SearchIndexEventMessage> searchIndexEventMessageProducerFactory) {
    return new KafkaTemplate<>(searchIndexEventMessageProducerFactory);
  }

  @Bean
  public ProducerFactory<String, SearchIndexEventMessage> searchIndexEventMessageProducerFactory() {
    Map<String, Object> configProps = new HashMap<>(kafkaProperties.buildProducerProperties(null));
    configProps.put(KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    configProps.put(VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
    return new DefaultKafkaProducerFactory<>(configProps);
  }

}
