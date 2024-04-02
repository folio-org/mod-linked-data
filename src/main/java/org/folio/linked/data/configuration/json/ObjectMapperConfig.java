package org.folio.linked.data.configuration.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.folio.linked.data.configuration.json.deserialization.ResourceFieldDeserializer;
import org.folio.linked.data.configuration.json.deserialization.event.DataImportEventDeserializer;
import org.folio.linked.data.configuration.json.deserialization.instance.InstanceReferenceTitleDeserializer;
import org.folio.linked.data.configuration.json.deserialization.instance.InstanceTitleDeserializer;
import org.folio.linked.data.configuration.json.deserialization.instance.MapDeserializer;
import org.folio.linked.data.configuration.json.deserialization.work.WorkReferenceTitleDeserializer;
import org.folio.linked.data.configuration.json.deserialization.work.WorkTitleDeserializer;
import org.folio.linked.data.configuration.json.serialization.MarcRecordSerializationConfig;
import org.folio.linked.data.domain.dto.InstanceAllOfMap;
import org.folio.linked.data.domain.dto.InstanceAllOfTitle;
import org.folio.linked.data.domain.dto.InstanceReferenceAllOfTitle;
import org.folio.linked.data.domain.dto.MarkRecord;
import org.folio.linked.data.domain.dto.ResourceField;
import org.folio.linked.data.domain.dto.WorkAllOfTitle;
import org.folio.linked.data.domain.dto.WorkReferenceAllOfTitle;
import org.folio.search.domain.dto.DataImportEvent;
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
      .setSerializationInclusion(JsonInclude.Include.NON_NULL)
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
      .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
      .registerModule(monographModule(mapper))
      .addMixIn(MarkRecord.class, MarcRecordSerializationConfig.class);
    return mapper;
  }

  private Module monographModule(ObjectMapper mapper) {
    var module = new SimpleModule();
    module.addDeserializer(ResourceField.class, new ResourceFieldDeserializer());
    module.addDeserializer(InstanceAllOfTitle.class, new InstanceTitleDeserializer());
    module.addDeserializer(InstanceReferenceAllOfTitle.class, new InstanceReferenceTitleDeserializer());
    module.addDeserializer(WorkAllOfTitle.class, new WorkTitleDeserializer());
    module.addDeserializer(WorkReferenceAllOfTitle.class, new WorkReferenceTitleDeserializer());
    module.addDeserializer(InstanceAllOfMap.class, new MapDeserializer());
    module.addDeserializer(DataImportEvent.class, new DataImportEventDeserializer(mapper));
    return module;
  }
}
