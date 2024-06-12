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
@Profile({FOLIO_PROFILE, SEARCH_PROFILE})
public class FolioKafkaConfig {
  private final KafkaProperties kafkaProperties;

  @Bean
  @Profile({FOLIO_PROFILE, SEARCH_PROFILE})
  public ProducerFactory<String, ResourceEvent> searchIndexProducerFactory() {
    return new DefaultKafkaProducerFactory<>(getProducerFactoryProperties());
  }

  @Bean
  @Profile(FOLIO_PROFILE)
  public ProducerFactory<String, InstanceIngressEvent> instanceIngressProducerFactory() {
    return new DefaultKafkaProducerFactory<>(getProducerFactoryProperties());
  }

  private Map<String, Object> getProducerFactoryProperties() {
    Map<String, Object> configProps = new HashMap<>(kafkaProperties.buildProducerProperties(null));
    configProps.put(KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    configProps.put(VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
    return configProps;
  }

  @Bean
  @Profile({FOLIO_PROFILE, SEARCH_PROFILE})
  public KafkaTemplate<String, ResourceEvent> searchTemplate(ProducerFactory<String, ResourceEvent> factory) {
    return new KafkaTemplate<>(factory);
  }

  @Bean
  @Profile(FOLIO_PROFILE)
  public KafkaTemplate<String, InstanceIngressEvent> iiTemplate(ProducerFactory<String, InstanceIngressEvent> factory) {
    return new KafkaTemplate<>(factory);
  }
}
