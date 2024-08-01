package org.folio.linked.data.util;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.experimental.UtilityClass;

@UtilityClass
public class JsonUtils {

  /**
   * Merges two JsonNode objects recursively combining the data from
   * the incoming into the existing.
   *
   * <p>Implemented for the specific case of resource properties merging,
   * may not work well in some extra complex cases.
   * When both are arrays, elements from the incoming are added to the existing array,
   * rather than replacing the existing elements. If a field exists only in one node,
   * it is added to the result. If a field is an array of objects with different fields,
   * they won't be combined to an array with single element and all fields inside.
   * This method ensures that the original nodes are not modified.</p>
   *
   * @param existing The original json. In case null the incoming node is returned.
   * @param incoming The incoming json. If null, the existing node is returned unchanged.
   * @return A JsonNode resulting from merging the existing and the incoming.
   */
  public static JsonNode merge(JsonNode existing, JsonNode incoming) {
    if (isNull(existing)) {
      return incoming;
    }
    if (isNull(incoming)) {
      return existing;
    }
    if (existing.isArray() && incoming.isArray()) {
      return mergeArrays((ArrayNode) existing, (ArrayNode) incoming);
    } else if (existing.isArray() || incoming.isArray()) {
      return existing;
    }

    var mergedNode = existing.<ObjectNode>deepCopy();
    var fieldNames = incoming.fieldNames();
    while (fieldNames.hasNext()) {
      var fieldName = fieldNames.next();
      var mainField = mergedNode.get(fieldName);
      var updateField = incoming.get(fieldName);

      if (nonNull(mainField) && mainField.isObject() && updateField.isObject()) {
        mergedNode.set(fieldName, merge(mainField, updateField));
      } else if (nonNull(mainField) && mainField.isArray() && updateField.isArray()) {
        mergedNode.set(fieldName, mergeArrays((ArrayNode) mainField, (ArrayNode) updateField));
      } else {
        mergedNode.set(fieldName, updateField);
      }
    }
    return mergedNode;
  }

  private static ArrayNode mergeArrays(ArrayNode existing, ArrayNode incoming) {
    var result = existing.deepCopy();
    for (var updateElement : incoming) {
      boolean found = false;

      for (int i = 0; i < result.size(); i++) {
        var mainElement = result.get(i);

        if (mainElement.isObject() && updateElement.isObject()) {
          var mainObjectNode = (ObjectNode) mainElement;
          var updateObjectNode = (ObjectNode) updateElement;
          var mainFieldNames = mainObjectNode.fieldNames();
          var updateFieldNames = updateObjectNode.fieldNames();

          if (mainFieldNames.hasNext() && updateFieldNames.hasNext()) {
            var mainKey = mainFieldNames.next();
            var updateKey = updateFieldNames.next();
            if (mainKey.equals(updateKey)) {
              result.set(i, merge(mainObjectNode, updateObjectNode));
              found = true;
              break;
            }
          }
        } else if (mainElement.equals(updateElement)) {
          found = true;
          break;
        }
      }
      if (!found) {
        result.add(updateElement);
      }
    }
    return result;
  }
}
