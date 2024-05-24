package org.folio.linked.data.configuration;

import static org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG;
import static org.folio.linked.data.util.Constants.FOLIO_PROFILE;
import static org.folio.linked.data.util.Constants.SEARCH_PROFILE;

import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.serialization.StringSerializer;
import org.folio.search.domain.dto.InstanceIngressEvent;
import org.folio.search.domain.dto.ResourceEvent;
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
public class SearchKafkaConfig {
  private final KafkaProperties kafkaProperties;

  @Bean
  public ProducerFactory<String, ResourceEvent> producerFactory() {
    Map<String, Object> configProps = new HashMap<>(kafkaProperties.buildProducerProperties(null));
    configProps.put(KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    configProps.put(VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
    return new DefaultKafkaProducerFactory<>(configProps);
  }

  @Bean
  @Profile(SEARCH_PROFILE)
  public KafkaTemplate<String, ResourceEvent> searchKafkaTemplate(ProducerFactory<String, ResourceEvent> producerFactory) {
    return new KafkaTemplate<>(producerFactory);
  }

  @Bean
  public KafkaTemplate<String, InstanceIngressEvent> inventoryKafkaTemplate(ProducerFactory<String, InstanceIngressEvent> producerFactory) {
    return new KafkaTemplate<>(producerFactory);
  }
}
