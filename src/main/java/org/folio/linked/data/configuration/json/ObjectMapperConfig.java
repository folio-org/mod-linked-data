package org.folio.linked.data.configuration.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.folio.linked.data.configuration.json.deserialization.ResourceRequestFieldDeserializer;
import org.folio.linked.data.configuration.json.deserialization.event.SourceRecordDomainEventDeserializer;
import org.folio.linked.data.configuration.json.deserialization.instance.IdentifierFieldDeserializer;
import org.folio.linked.data.configuration.json.deserialization.title.TitleFieldRequestDeserializer;
import org.folio.linked.data.configuration.json.serialization.MarcRecordSerializationConfig;
import org.folio.linked.data.domain.dto.IdentifierField;
import org.folio.linked.data.domain.dto.MarcRecord;
import org.folio.linked.data.domain.dto.ResourceRequestField;
import org.folio.linked.data.domain.dto.SourceRecordDomainEvent;
import org.folio.linked.data.domain.dto.TitleFieldRequestTitleInner;
import org.folio.linked.data.exception.RequestProcessingExceptionBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class ObjectMapperConfig {

  @Bean
  @Primary
  public ObjectMapper objectMapper(RequestProcessingExceptionBuilder exceptionBuilder) {
    var om = new ObjectMapper()
      .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
      .configure(SerializationFeature.USE_EQUALITY_FOR_OBJECT_ID, true)
      .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
      .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
      .addMixIn(MarcRecord.class, MarcRecordSerializationConfig.class);
    om.registerModule(new JavaTimeModule());
    om.registerModule(monographModule(om, exceptionBuilder));
    return om;
  }

  private Module monographModule(ObjectMapper objectMapper, RequestProcessingExceptionBuilder exceptionBuilder) {
    return new SimpleModule()
      .addDeserializer(ResourceRequestField.class, new ResourceRequestFieldDeserializer(exceptionBuilder))
      .addDeserializer(TitleFieldRequestTitleInner.class, new TitleFieldRequestDeserializer(exceptionBuilder))
      .addDeserializer(IdentifierField.class, new IdentifierFieldDeserializer(exceptionBuilder))
      .addDeserializer(SourceRecordDomainEvent.class, new SourceRecordDomainEventDeserializer(objectMapper));
  }

}
