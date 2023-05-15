package org.folio.linked.data.mapper;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.folio.linked.data.domain.dto.BibframeCreateRequest;
import org.folio.linked.data.domain.dto.BibframeResponse;
import org.folio.linked.data.model.entity.Bibframe;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = SPRING, imports = {Bibframe.class, BibframeCreateRequest.class, BibframeResponse.class})
public interface BibframeMapper {

  ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Mapping(source = "configuration", target = "configuration", qualifiedByName = "objectToJson")
  Bibframe map(BibframeCreateRequest bibframeCreateRequest);

  BibframeResponse map(Bibframe bibframe);

  @Named("objectToJson")
  default JsonNode toJson(Object configuration) throws JsonProcessingException {
    JsonNode node = null;
    if (configuration instanceof String json) {
      node = OBJECT_MAPPER.readTree(json);
    } else if (configuration instanceof Map json) {
      node = OBJECT_MAPPER.convertValue(json, JsonNode.class);
    }
    return node != null ? node : OBJECT_MAPPER.readTree("{}");
  }


}
