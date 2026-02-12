package org.folio.linked.data.configuration.kafka;

import static org.folio.linked.data.util.Constants.STANDALONE_PROFILE;
import static org.folio.linked.data.util.JsonUtils.JSON_MAPPER;

import java.util.HashMap;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.folio.linked.data.domain.dto.ImportOutputEvent;
import org.folio.linked.data.domain.dto.InventoryInstanceEvent;
import org.folio.linked.data.domain.dto.SourceRecordDomainEvent;
import org.folio.spring.tools.kafka.FolioKafkaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;

@Configuration
@RequiredArgsConstructor
@Profile("!" + STANDALONE_PROFILE)
public class KafkaListenerConfiguration {

  @Bean
  @ConfigurationProperties("folio.kafka")
  public FolioKafkaProperties folioKafkaProperties() {
    return new FolioKafkaProperties();
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, SourceRecordDomainEvent> srsEventListenerContainerFactory(
    ConsumerFactory<String, SourceRecordDomainEvent> sourceRecordDomainEventConsumerFactory
  ) {
    return concurrentKafkaBatchListenerContainerFactory(sourceRecordDomainEventConsumerFactory);
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, InventoryInstanceEvent> inventoryEventListenerContainerFactory(
    ConsumerFactory<String, InventoryInstanceEvent> inventoryInstanceEventConsumerFactory
  ) {
    return concurrentKafkaBatchListenerContainerFactory(inventoryInstanceEventConsumerFactory);
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, ImportOutputEvent> importOutputEventListenerContainerFactory(
    ConsumerFactory<String, ImportOutputEvent> ldImportOutputEventConsumerFactory
  ) {
    return concurrentKafkaBatchListenerContainerFactory(ldImportOutputEventConsumerFactory);
  }

  @Bean
  public ConsumerFactory<String, SourceRecordDomainEvent> srsDomainEventConsumerFactory(KafkaProperties properties) {
    return errorHandlingConsumerFactory(SourceRecordDomainEvent.class, properties);
  }

  @Bean
  public ConsumerFactory<String, InventoryInstanceEvent> inventoryEventConsumerFactory(KafkaProperties properties) {
    return errorHandlingConsumerFactory(InventoryInstanceEvent.class, properties);
  }

  @Bean
  public ConsumerFactory<String, ImportOutputEvent> ldImportOutputEventConsumerFactory(KafkaProperties properties) {
    return errorHandlingConsumerFactory(ImportOutputEvent.class, properties);
  }


  private <V> ConcurrentKafkaListenerContainerFactory<String, V> concurrentKafkaBatchListenerContainerFactory(
    ConsumerFactory<String, V> consumerFactory) {
    var factory = new ConcurrentKafkaListenerContainerFactory<String, V>();
    factory.setBatchListener(true);
    factory.setConsumerFactory(consumerFactory);
    return factory;
  }


  private <V> ConsumerFactory<String, V> errorHandlingConsumerFactory(Class<V> clazz,
                                                                      KafkaProperties kafkaProperties) {
    var properties = new HashMap<>(kafkaProperties.buildConsumerProperties());
    Supplier<Deserializer<String>> keyDeserializer = StringDeserializer::new;
    Supplier<Deserializer<V>> valueDeserializer = () ->
      new ErrorHandlingDeserializer<>(new JacksonJsonDeserializer<>(clazz, JSON_MAPPER));
    return new DefaultKafkaConsumerFactory<>(properties, keyDeserializer, valueDeserializer);
  }
}
