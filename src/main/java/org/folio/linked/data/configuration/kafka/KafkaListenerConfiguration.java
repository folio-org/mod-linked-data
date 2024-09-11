package org.folio.linked.data.configuration.kafka;

import static org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG;
import static org.folio.linked.data.util.Constants.FOLIO_PROFILE;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.folio.linked.data.domain.dto.SourceRecordDomainEvent;
import org.folio.spring.tools.kafka.FolioKafkaProperties;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

@Configuration
@RequiredArgsConstructor
@Profile(FOLIO_PROFILE)
public class KafkaListenerConfiguration {

  private final KafkaProperties kafkaProperties;
  private final ObjectMapper mapper;

  @Bean
  @ConfigurationProperties("folio.kafka")
  public FolioKafkaProperties folioKafkaProperties() {
    return new FolioKafkaProperties();
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, SourceRecordDomainEvent> srsEventListenerContainerFactory(
    ConsumerFactory<String, SourceRecordDomainEvent> sourceRecordDomainEventConsumerFactory
  ) {
    var factory = new ConcurrentKafkaListenerContainerFactory<String, SourceRecordDomainEvent>();
    factory.setBatchListener(true);
    factory.setConsumerFactory(sourceRecordDomainEventConsumerFactory);
    return factory;
  }

  @Bean
  public ConsumerFactory<String, SourceRecordDomainEvent> sourceRecordDomainEventConsumerFactory() {
    var deserializer = new ErrorHandlingDeserializer<>(new JsonDeserializer<>(SourceRecordDomainEvent.class, mapper));
    Map<String, Object> config = new HashMap<>(kafkaProperties.buildConsumerProperties(null));
    config.put(KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    config.put(VALUE_DESERIALIZER_CLASS_CONFIG, deserializer);
    return new DefaultKafkaConsumerFactory<>(config, new StringDeserializer(), deserializer);
  }

}
