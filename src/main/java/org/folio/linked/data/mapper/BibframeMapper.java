package org.folio.linked.data.mapper;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.folio.linked.data.domain.dto.BibframeCreateRequest;
import org.folio.linked.data.domain.dto.BibframeResponse;
import org.folio.linked.data.model.entity.Bibframe;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = SPRING, imports = {Bibframe.class, BibframeCreateRequest.class, BibframeResponse.class})
public interface BibframeMapper {

  ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Mapping(source = "configuration", target = "configuration", qualifiedByName = "stringToJson")
  Bibframe map(BibframeCreateRequest bibframeCreateRequest);

  @Mapping(source = "configuration", target = "configuration", qualifiedByName = "jsonToString")
  BibframeResponse map(Bibframe bibframe);

  @Named("stringToJson")
  default JsonNode toJson(String configuration) throws JsonProcessingException {
    return OBJECT_MAPPER.readTree(configuration);
  }

  @Named("jsonToString")
  default String toString(JsonNode configuration) throws JsonProcessingException {
    return OBJECT_MAPPER.writeValueAsString(configuration);
  }

}
