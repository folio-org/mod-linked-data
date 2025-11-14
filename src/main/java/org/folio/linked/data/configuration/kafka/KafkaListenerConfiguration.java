package org.folio.linked.data.configuration.kafka;

import static org.folio.linked.data.util.Constants.STANDALONE_PROFILE;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.function.Supplier;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.folio.linked.data.domain.dto.ImportOutputEvent;
import org.folio.linked.data.domain.dto.InventoryInstanceEvent;
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
    return concurrentKafkaBatchListenerContainerFactory(sourceRecordDomainEventConsumerFactory, true);
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, InventoryInstanceEvent> inventoryEventListenerContainerFactory(
    ConsumerFactory<String, InventoryInstanceEvent> inventoryInstanceEventConsumerFactory
  ) {
    return concurrentKafkaBatchListenerContainerFactory(inventoryInstanceEventConsumerFactory, true);
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, ImportOutputEvent> importOutputEventListenerContainerFactory(
    ConsumerFactory<String, ImportOutputEvent> ldImportOutputEventConsumerFactory
  ) {
    return concurrentKafkaBatchListenerContainerFactory(ldImportOutputEventConsumerFactory, false);
  }

  @Bean
  public ConsumerFactory<String, SourceRecordDomainEvent> srsDomainEventConsumerFactory(ObjectMapper mapper,
                                                                                        KafkaProperties properties) {
    return errorHandlingConsumerFactory(SourceRecordDomainEvent.class, mapper, properties);
  }

  @Bean
  public ConsumerFactory<String, InventoryInstanceEvent> inventoryEventConsumerFactory(ObjectMapper mapper,
                                                                                       KafkaProperties properties) {
    return errorHandlingConsumerFactory(InventoryInstanceEvent.class, mapper, properties);
  }

  @Bean
  public ConsumerFactory<String, ImportOutputEvent> ldImportOutputEventConsumerFactory(ObjectMapper mapper,
                                                                                       KafkaProperties properties) {
    return errorHandlingConsumerFactory(ImportOutputEvent.class, mapper, properties);
  }


  private <V> ConcurrentKafkaListenerContainerFactory<String, V> concurrentKafkaBatchListenerContainerFactory(
    ConsumerFactory<String, V> consumerFactory, boolean batch) {
    var factory = new ConcurrentKafkaListenerContainerFactory<String, V>();
    factory.setBatchListener(batch);
    factory.setConsumerFactory(consumerFactory);
    return factory;
  }


  private <V> ConsumerFactory<String, V> errorHandlingConsumerFactory(Class<V> clazz,
                                                                      ObjectMapper mapper,
                                                                      KafkaProperties kafkaProperties) {
    var properties = new HashMap<>(kafkaProperties.buildConsumerProperties(null));
    Supplier<Deserializer<String>> keyDeserializer = StringDeserializer::new;
    Supplier<Deserializer<V>> valueDeserializer = () ->
      new ErrorHandlingDeserializer<>(new JsonDeserializer<>(clazz, mapper));
    return new DefaultKafkaConsumerFactory<>(properties, keyDeserializer, valueDeserializer);
  }
}
