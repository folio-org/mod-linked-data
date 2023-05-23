package org.folio.linked.data.mapper;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.folio.linked.data.domain.dto.BibframeCreateRequest;
import org.folio.linked.data.domain.dto.BibframeResponse;
import org.folio.linked.data.domain.dto.BibframeShort;
import org.folio.linked.data.domain.dto.BibframeShortInfoPage;
import org.folio.linked.data.domain.dto.BibframeUpdateRequest;
import org.folio.linked.data.model.BibframeIdAndGraphName;
import org.folio.linked.data.model.entity.Bibframe;
import org.folio.linked.data.util.TextUtil;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

@Mapper(componentModel = SPRING, imports = {TextUtil.class})
public abstract class BibframeMapper {

  @Autowired
  private ObjectMapper objectMapper;

  @Mapping(target = "slug", expression = "java(TextUtil.slugify(bibframeCreateRequest.getGraphName()))")
  @Mapping(target = "graphHash", expression = "java(bibframe.getSlug().hashCode())")
  @Mapping(target = "configuration", source = "configuration", qualifiedByName = "objectToJson")
  public abstract Bibframe map(BibframeCreateRequest bibframeCreateRequest);

  public abstract BibframeResponse map(Bibframe bibframe);

  public abstract BibframeShort map(BibframeIdAndGraphName bibframeIdAndGraphName);

  public abstract BibframeShortInfoPage map(Page<BibframeShort> page);

  @Mapping(target = "configuration", source = "configuration", qualifiedByName = "objectToJson")
  public abstract Bibframe update(@MappingTarget Bibframe entity, BibframeUpdateRequest updateRequest);

  @Named("objectToJson")
  protected JsonNode toJson(Object configuration) throws JsonProcessingException {
    JsonNode node = null;
    if (configuration instanceof String json) {
      node = objectMapper.readTree(json);
    } else if (configuration instanceof Map json) {
      node = objectMapper.convertValue(json, JsonNode.class);
    }
    return node != null ? node : objectMapper.readTree("{}");
  }
}
