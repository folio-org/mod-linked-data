package org.folio.linked.data.configuration.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.folio.linked.data.configuration.json.deserialization.ResourceFieldDeserializer;
import org.folio.linked.data.configuration.json.deserialization.event.DataImportEventDeserializer;
import org.folio.linked.data.configuration.json.deserialization.instance.AgentDeserializer;
import org.folio.linked.data.configuration.json.deserialization.instance.MapDeserializer;
import org.folio.linked.data.configuration.json.deserialization.instance.TitleDeserializer;
import org.folio.linked.data.domain.dto.AgentTypeInner;
import org.folio.linked.data.domain.dto.InstanceAllOfMapInner;
import org.folio.linked.data.domain.dto.InstanceAllOfTitleInner;
import org.folio.linked.data.domain.dto.ResourceField;
import org.folio.search.domain.dto.DataImportEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Primary
@Configuration
public class ObjectMapperConfig {

  @Bean
  public ObjectMapper objectMapper() {
    ObjectMapper mapper = new ObjectMapper();
    mapper
      .setSerializationInclusion(JsonInclude.Include.NON_NULL)
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
      .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
      .registerModule(monographModule(mapper));
    return mapper;
  }

  private Module monographModule(ObjectMapper mapper) {
    var module = new SimpleModule();
    module.addDeserializer(ResourceField.class, new ResourceFieldDeserializer());
    module.addDeserializer(InstanceAllOfTitleInner.class, new TitleDeserializer());
    module.addDeserializer(InstanceAllOfMapInner.class, new MapDeserializer());
    module.addDeserializer(AgentTypeInner.class, new AgentDeserializer());
    module.addDeserializer(DataImportEvent.class, new DataImportEventDeserializer(mapper));
    return module;
  }
}
