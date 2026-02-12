package org.folio.linked.data.util;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.exception.JsonMappingException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;

@RequiredArgsConstructor
public class DtoDeserializer<D> {

  private final Class<D> dtoClass;
  private final Map<String, Class<? extends D>> identityMap;

  public D deserialize(JsonParser jp, DeserializationContext dc) {
    try {
      JsonNode node = jp.readValueAsTree();
      return identityMap.entrySet()
        .stream()
        .filter(entry -> node.has(entry.getKey()))
        .map(Map.Entry::getValue)
        .map(clazz -> dc.readTreeAsValue(node, clazz))
        .findFirst()
        .orElseThrow(() -> new JsonMappingException(dtoClass.getSimpleName(), String.valueOf(node)));
    } catch (Exception e) {
      throw new JsonMappingException(dtoClass.getSimpleName(), "Failed to parse JSON: " + e.getMessage());
    }
  }

}
