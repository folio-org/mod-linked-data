package org.folio.linked.data.mapper;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import java.util.Map;
import org.folio.linked.data.domain.dto.BibframeResponse;
import org.folio.linked.data.domain.dto.BibframeShort;
import org.folio.linked.data.domain.dto.BibframeShortInfoPage;
import org.folio.linked.data.exception.JsonException;
import org.folio.linked.data.model.BibframeIdAndGraphName;
import org.folio.linked.data.util.TextUtil;
import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

@Mapper(componentModel = SPRING, imports = {TextUtil.class})
public abstract class BibframeMapper {

  private static final String ERROR_JSON_PROCESSING = "Error while json processing";

  @Autowired
  private ObjectMapper objectMapper;

  public abstract BibframeShort map(BibframeIdAndGraphName bibframeIdAndGraphName);

  public abstract BibframeShortInfoPage map(Page<BibframeShort> page);

  @Named("objectToJson")
  public JsonNode toJson(Object configuration) {
    try {
      JsonNode node = null;
      if (configuration instanceof String json) {
        node = objectMapper.readTree(json);
      } else if (configuration instanceof Map json) {
        node = objectMapper.convertValue(json, JsonNode.class);
      } else {
        node = objectMapper.valueToTree(configuration);
      }
      return !(node instanceof NullNode) ? node : objectMapper.createObjectNode();
    } catch (JsonProcessingException e) {
      throw new JsonException(ERROR_JSON_PROCESSING, e);
    }
  }

  @Named("jsonToBibframe")
  public BibframeResponse toBibframe(JsonNode node) {
    return node != null
        ? objectMapper.convertValue(node, BibframeResponse.class)
        : new BibframeResponse();
  }


}
