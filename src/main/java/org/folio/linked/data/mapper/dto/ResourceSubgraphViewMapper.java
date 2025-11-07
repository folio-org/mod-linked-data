package org.folio.linked.data.mapper.dto;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.io.IOException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.ld.dictionary.model.Resource;
import org.folio.ld.dictionary.util.ResourceViewDeserializer;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class ResourceSubgraphViewMapper {

  private final ObjectMapper objectMapper = new ObjectMapper()
    .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    .registerModule(new SimpleModule()
      .addDeserializer(Resource.class, new ResourceViewDeserializer())
    );

  public Optional<Resource> fromJson(String jsonGraph) {
    try {
      return Optional.of(objectMapper.readValue(jsonGraph, Resource.class));
    } catch (IOException e) {
      log.error("Failed to convert resource graph JSON to DTO: {}", jsonGraph, e);
      return Optional.empty();
    }
  }

}
