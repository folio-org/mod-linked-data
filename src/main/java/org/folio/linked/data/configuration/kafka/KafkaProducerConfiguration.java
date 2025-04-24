package org.folio.linked.data.configuration.kafka;

import static org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG;
import static org.folio.linked.data.util.Constants.STANDALONE_PROFILE;

import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.serialization.StringSerializer;
import org.folio.linked.data.domain.dto.InstanceIngressEvent;
import org.folio.linked.data.domain.dto.LinkedDataWork;
import org.folio.linked.data.domain.dto.ResourceIndexEvent;
import org.folio.spring.tools.kafka.FolioMessageProducer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.lang.NonNull;

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
  public FolioMessageProducer<InstanceIngressEvent> instanceIngressEventProducer(
    KafkaTemplate<String, InstanceIngressEvent> instanceIngressMessageTemplate
  ) {
    var producer = new FolioMessageProducer<>(instanceIngressMessageTemplate,
      (FolioKafkaTopicWithNamespace) linkedDataTopicProperties::getInstanceIngress);
    producer.setKeyMapper(iie -> iie.getEventPayload().getSourceRecordIdentifier());
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
  public ProducerFactory<String, ResourceIndexEvent> resourceIndexEventProducerFactory() {
    return new DefaultKafkaProducerFactory<>(getProducerProperties());
  }

  @Bean
  public ProducerFactory<String, InstanceIngressEvent> instanceIngressEventProducerFactory() {
    return new DefaultKafkaProducerFactory<>(getProducerProperties());
  }

  private @NonNull Map<String, Object> getProducerProperties() {
    Map<String, Object> configProps = new HashMap<>(kafkaProperties.buildProducerProperties(null));
    configProps.put(KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    configProps.put(VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
    return configProps;
  }
}
