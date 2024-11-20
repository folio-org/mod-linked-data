package org.folio.linked.data.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.exception.RequestProcessingExceptionBuilder;

@RequiredArgsConstructor
public class DtoDeserializer<D> {

  private final Class<D> dtoClass;
  private final Map<String, Class<? extends D>> identityMap;
  private final RequestProcessingExceptionBuilder exceptionBuilder;

  public D deserialize(JsonParser jp)
    throws IOException {
    JsonNode node = jp.getCodec().readTree(jp);
    return identityMap.entrySet()
      .stream()
      .filter(entry -> node.has(entry.getKey()))
      .map(Map.Entry::getValue)
      .map(clazz -> deserialize(jp, node, clazz))
      .findFirst()
      .orElseThrow(() -> exceptionBuilder.mappingException(dtoClass.getSimpleName(), String.valueOf(node)));
  }


  private <T> T deserialize(JsonParser jp, JsonNode node, Class<T> clazz) {
    try {
      return jp.getCodec().treeToValue(node, clazz);
    } catch (JsonProcessingException e) {
      throw exceptionBuilder.mappingException(clazz.getSimpleName(), String.valueOf(node));
    }
  }

}
