package org.folio.linked.data.configuration.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.folio.linked.data.configuration.json.deserialization.ResourceRequestFieldDeserializer;
import org.folio.linked.data.configuration.json.deserialization.instance.InstanceRequestAllOfMapDeserializer;
import org.folio.linked.data.configuration.json.deserialization.title.TitleFieldRequestDeserializer;
import org.folio.linked.data.configuration.json.serialization.MarcRecordSerializationConfig;
import org.folio.linked.data.domain.dto.InstanceRequestAllOfMap;
import org.folio.linked.data.domain.dto.MarcRecord;
import org.folio.linked.data.domain.dto.ResourceRequestField;
import org.folio.linked.data.domain.dto.TitleFieldRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Primary
@Configuration
public class ObjectMapperConfig {

  @Bean
  public ObjectMapper objectMapper() {
    var mapper = new ObjectMapper();
    mapper
      .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
      .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
      .registerModule(monographModule(mapper))
      .addMixIn(MarcRecord.class, MarcRecordSerializationConfig.class);
    return mapper;
  }

  private Module monographModule(ObjectMapper mapper) {
    var module = new SimpleModule();
    module.addDeserializer(ResourceRequestField.class, new ResourceRequestFieldDeserializer());
    module.addDeserializer(TitleFieldRequest.class, new TitleFieldRequestDeserializer());
    module.addDeserializer(InstanceRequestAllOfMap.class, new InstanceRequestAllOfMapDeserializer());
    return module;
  }
}
