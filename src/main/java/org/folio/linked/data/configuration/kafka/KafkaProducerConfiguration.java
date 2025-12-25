package org.folio.linked.data.configuration.kafka;

import static org.folio.linked.data.util.Constants.STANDALONE_PROFILE;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.folio.linked.data.domain.dto.ImportResultEvent;
import org.folio.linked.data.domain.dto.InstanceIngressEvent;
import org.folio.linked.data.domain.dto.LinkedDataHub;
import org.folio.linked.data.domain.dto.LinkedDataWork;
import org.folio.linked.data.domain.dto.ResourceIndexEvent;
import org.folio.spring.tools.kafka.FolioMessageProducer;
import org.jetbrains.annotations.NotNull;
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
@Profile("!" + STANDALONE_PROFILE)
public class KafkaProducerConfiguration {
  private final KafkaProperties kafkaProperties;
  private final LinkedDataTopicProperties linkedDataTopicProperties;

  @Bean
  public FolioMessageProducer<ResourceIndexEvent> bibliographicMessageProducer(
    KafkaTemplate<String, ResourceIndexEvent> resourceIndexEventMessageTemplate
  ) {
    var producer = new FolioMessageProducer<>(resourceIndexEventMessageTemplate,
      linkedDataTopicProperties::getWorkSearchIndex);
    producer.setKeyMapper(rie -> ((LinkedDataWork) rie.getNew()).getId());
    return producer;
  }

  @Bean
  public FolioMessageProducer<ResourceIndexEvent> hubIndexMessageProducer(
    KafkaTemplate<String, ResourceIndexEvent> resourceIndexEventMessageTemplate
  ) {
    var producer = new FolioMessageProducer<>(resourceIndexEventMessageTemplate,
      linkedDataTopicProperties::getHubSearchIndex);
    producer.setKeyMapper(rie -> ((LinkedDataHub) rie.getNew()).getId());
    return producer;
  }

  @Bean
  public FolioMessageProducer<InstanceIngressEvent> instanceIngressEventProducer(
    KafkaTemplate<String, InstanceIngressEvent> instanceIngressMessageTemplate
  ) {
    var producer = new FolioMessageProducer<>(instanceIngressMessageTemplate,
      (FolioKafkaTopicWithNamespace) linkedDataTopicProperties::getInstanceIngress);
    producer.setKeyMapper(iie -> iie.getEventPayload().getSourceRecordIdentifier());
    return producer;
  }

  @Bean
  public FolioMessageProducer<ImportResultEvent> importResultEventProducer(
    KafkaTemplate<String, ImportResultEvent> importResultEventTemplate
  ) {
    var producer = new FolioMessageProducer<>(importResultEventTemplate,
      linkedDataTopicProperties::getLinkedDataImportResult);
    producer.setKeyMapper(result -> String.valueOf(result.getJobExecutionId()));
    return producer;
  }

  @Bean
  public KafkaTemplate<String, ResourceIndexEvent> resourceIndexEventTemplate(
    ProducerFactory<String, ResourceIndexEvent> resourceIndexEventMessageProducerFactory) {
    return new KafkaTemplate<>(resourceIndexEventMessageProducerFactory);
  }

  @Bean
  public KafkaTemplate<String, InstanceIngressEvent> instanceIngressEventTemplate(
    ProducerFactory<String, InstanceIngressEvent> instanceIngressMessageProducerFactory
  ) {
    return new KafkaTemplate<>(instanceIngressMessageProducerFactory);
  }

  @Bean
  public KafkaTemplate<String, ImportResultEvent> importResultEventTemplate(
    ProducerFactory<String, ImportResultEvent> importResultEventProducerFactory
  ) {
    return new KafkaTemplate<>(importResultEventProducerFactory);
  }

  @Bean
  public ProducerFactory<String, ResourceIndexEvent> resourceIndexEventProducerFactory(ObjectMapper objectMapper) {
    return getKafkaProducerFactory(objectMapper);
  }

  @Bean
  public ProducerFactory<String, InstanceIngressEvent> instanceIngressEventProducerFactory(ObjectMapper objectMapper) {
    return getKafkaProducerFactory(objectMapper);
  }

  @Bean
  public ProducerFactory<String, ImportResultEvent> importResultEventProducerFactory(
    ObjectMapper objectMapper) {
    return getKafkaProducerFactory(objectMapper);
  }

  private @NotNull <T> DefaultKafkaProducerFactory<String, T> getKafkaProducerFactory(ObjectMapper objectMapper) {
    var properties = new HashMap<>(kafkaProperties.buildProducerProperties(null));
    Supplier<Serializer<String>> keySerializer = StringSerializer::new;
    Supplier<Serializer<T>> valueSerializer = () -> new JsonSerializer<>(objectMapper);
    return new DefaultKafkaProducerFactory<>(properties, keySerializer, valueSerializer);
  }
}
