package org.folio.linked.data.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;

@Log4j2
@UtilityClass
public class JsonUtils {

  @SneakyThrows
  public static String writeValueAsString(Object obj, ObjectMapper objectMapper) {
    return obj instanceof String str ? str : objectMapper.writeValueAsString(obj);
  }

  /**
   * Merges two JsonNode objects combining the data from
   * the incoming into the existing.
   *
   * <p>Implemented for the specific case of resource properties merging.
   * When both are arrays, elements from the incoming are added to the existing array,
   * rather than replacing the existing elements. If a field exists only in one node,
   * it is added to the result.
   * This method ensures that the original nodes are not modified.</p>
   *
   * @param existing The original json. In case null the incoming node is returned.
   * @param incoming The incoming json. If null, the existing node is returned unchanged.
   * @return A JsonNode resulting from merging the existing and the incoming.
   */
  public static JsonNode merge(JsonNode existing, JsonNode incoming) {
    try {
      return mergeNodes(existing, incoming);
    } catch (Exception e) {
      log.error("And exception occurred on merging JSON properties, the docs have different structure");
    }
    return existing;
  }

  private static JsonNode mergeNodes(JsonNode existing, JsonNode incoming) {
    if (isNull(existing)) {
      return isObject(incoming) ? incoming : null;
    }
    if (isNull(incoming)) {
      return existing;
    }
    if (anyNonObject(existing, incoming)) {
      return existing;
    }
    var copyOfExisting = existing.<ObjectNode>deepCopy();
    incoming.fieldNames().forEachRemaining(incomingFieldName -> {
      var existingField = copyOfExisting.get(incomingFieldName);
      var incomingField = incoming.get(incomingFieldName);
      if (isNull(existingField) && nonNull(incomingField)) {
        copyOfExisting.set(incomingFieldName, incomingField.deepCopy());
      } else if (allArray(existingField, incomingField)) {
        copyOfExisting.set(incomingFieldName, mergeArrays((ArrayNode) existingField, (ArrayNode) incomingField));
      }
    });
    return copyOfExisting;
  }

  private static ArrayNode mergeArrays(ArrayNode existingArray, ArrayNode incomingArray) {
    var arrayCopy = existingArray.deepCopy();
    incomingArray.forEach(element -> addIfNotExist(arrayCopy, element));
    return arrayCopy;
  }

  private static void addIfNotExist(ArrayNode array, JsonNode node) {
    if (node.isTextual() && arrayNotContainsElement(array, node)) {
      array.add(node);
    }
  }

  private static boolean arrayNotContainsElement(ArrayNode array, JsonNode value) {
    return StreamSupport.stream(array.spliterator(), false)
      .filter(JsonNode::isTextual)
      .map(JsonNode::asText)
      .noneMatch(text -> text.equals(value.asText()));
  }

  private static boolean allArray(JsonNode... nodes) {
    return Stream.of(nodes).allMatch(JsonUtils::isArray);
  }

  private static boolean anyNonObject(JsonNode... nodes) {
    return !Stream.of(nodes).allMatch(JsonUtils::isObject);
  }

  private static boolean isObject(JsonNode node) {
    return nonNull(node) && node.isObject();
  }

  private static boolean isArray(JsonNode node) {
    return nonNull(node) && node.isArray();
  }

  private static boolean isNull(JsonNode node) {
    return Objects.isNull(node) || node.isNull();
  }

  private static boolean nonNull(JsonNode node) {
    return Objects.nonNull(node) && !node.isNull();
  }
}
