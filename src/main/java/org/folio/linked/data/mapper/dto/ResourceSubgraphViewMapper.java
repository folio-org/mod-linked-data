package org.folio.linked.data.mapper.dto;

import static org.folio.linked.data.util.JsonUtils.JSON_MAPPER;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.ld.dictionary.model.Resource;
import org.folio.ld.dictionary.util.ResourceViewDeserializer;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

@Log4j2
@Component
@RequiredArgsConstructor
public class ResourceSubgraphViewMapper {

  private final JsonMapper jsonMapper = JSON_MAPPER.rebuild()
    .addModule(new SimpleModule().addDeserializer(Resource.class, new ResourceViewDeserializer()))
    .build();

  public Optional<Resource> fromJson(String jsonGraph) {
    try {
      return Optional.of(jsonMapper.readValue(jsonGraph, Resource.class));
    } catch (JacksonException e) {
      log.error("Failed to convert resource graph JSON to DTO: {}", jsonGraph, e);
      return Optional.empty();
    }
  }

}
