package org.folio.linked.data.configuration;

import static org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG;
import static org.folio.linked.data.util.Constants.FOLIO_PROFILE;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.folio.search.domain.dto.DataImportEvent;
import org.folio.spring.tools.kafka.FolioKafkaProperties;
import org.folio.spring.tools.systemuser.PrepareSystemUserService;
import org.folio.spring.tools.systemuser.SystemUserService;
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
import org.springframework.retry.support.RetryTemplate;

@Configuration
@RequiredArgsConstructor
@Profile(FOLIO_PROFILE)
public class KafkaListenerConfiguration {

  private final KafkaProperties kafkaProperties;
  private final ObjectMapper objectMapper;

  @Bean
  @ConfigurationProperties("folio.kafka")
  public FolioKafkaProperties folioKafkaProperties() {
    return new FolioKafkaProperties();
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, DataImportEvent> dataImportListenerContainerFactory() {
    var factory = new ConcurrentKafkaListenerContainerFactory<String, DataImportEvent>();
    factory.setBatchListener(true);
    factory.setConsumerFactory(dataImportEventConsumerFactory());
    return factory;
  }

  @Bean
  public ConsumerFactory<String, DataImportEvent> dataImportEventConsumerFactory() {
    var deserializer = new ErrorHandlingDeserializer<>(new JsonDeserializer<>(DataImportEvent.class, objectMapper));
    Map<String, Object> config = new HashMap<>(kafkaProperties.buildConsumerProperties());
    config.put(KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    config.put(VALUE_DESERIALIZER_CLASS_CONFIG, deserializer);
    return new DefaultKafkaConsumerFactory<>(config, new StringDeserializer(), deserializer);
  }

  @Bean("defaultRetryTemplate")
  public RetryTemplate retryTemplate() {
    return new RetryTemplate();
  }

  @Bean("folioPrepareSystemUserService")
  public PrepareSystemUserService dummyPrepareSystemUserService() {
    return null;
  }

  @Bean("folioSystemUserService")
  public SystemUserService dummySystemUserService() {
    return null;
  }

}
