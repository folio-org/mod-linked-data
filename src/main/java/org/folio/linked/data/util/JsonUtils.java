package org.folio.linked.data.util;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
import static org.apache.commons.lang3.StringUtils.isAnyBlank;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.configuration.json.deserialization.ResourceRequestFieldDeserializer;
import org.folio.linked.data.configuration.json.deserialization.event.SourceRecordDomainEventDeserializer;
import org.folio.linked.data.configuration.json.deserialization.instance.IdentifierFieldDeserializer;
import org.folio.linked.data.configuration.json.deserialization.title.TitleFieldRequestDeserializer;
import org.folio.linked.data.configuration.json.serialization.MarcRecordSerializationConfig;
import org.folio.linked.data.domain.dto.IdentifierField;
import org.folio.linked.data.domain.dto.MarcRecord;
import org.folio.linked.data.domain.dto.ResourceRequestField;
import org.folio.linked.data.domain.dto.SourceRecordDomainEvent;
import org.folio.linked.data.domain.dto.TitleFieldRequestTitleInner;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

@Log4j2
@UtilityClass
public class JsonUtils {

  public static final JsonMapper JSON_MAPPER = JsonMapper.builder()
    .changeDefaultPropertyInclusion(incl -> incl.withValueInclusion(NON_EMPTY))
    .configure(SerializationFeature.USE_EQUALITY_FOR_OBJECT_ID, true)
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
    .addMixIn(MarcRecord.class, MarcRecordSerializationConfig.class)
    .addModule(new SimpleModule()
      .addDeserializer(ResourceRequestField.class, new ResourceRequestFieldDeserializer())
      .addDeserializer(TitleFieldRequestTitleInner.class, new TitleFieldRequestDeserializer())
      .addDeserializer(IdentifierField.class, new IdentifierFieldDeserializer())
      .addDeserializer(SourceRecordDomainEvent.class, new SourceRecordDomainEventDeserializer()))
    .build();

  private static final Configuration JSONPATH_CONFIG = Configuration.builder()
    .options(Option.SUPPRESS_EXCEPTIONS)
    .build();

  public static boolean hasElementByJsonPath(String json, String jsonPath) {
    if (isAnyBlank(json, jsonPath)) {
      return false;
    }
    return !JsonPath.using(JSONPATH_CONFIG).parse(json).read(jsonPath, List.class).isEmpty();
  }

  public static <T> Optional<T> getElementByJsonPath(String json, String jsonPath, Class<T> type) {
    if (isAnyBlank(json, jsonPath) || Objects.isNull(type)) {
      return Optional.empty();
    }
    return Optional.ofNullable(JsonPath.using(JSONPATH_CONFIG).parse(json).read(jsonPath, type));
  }

  public static String writeValueAsString(Object obj) {
    return obj instanceof String str ? str : JSON_MAPPER.writeValueAsString(obj);
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

  public static void setProperty(JsonNode existing, String property, JsonNode value) {
    if (existing.isObject()) {
      ((ObjectNode) existing).set(property, value);
    }
  }

  public static Optional<JsonNode> getProperty(JsonNode node, String property) {
    return Optional.ofNullable(node.get(property));
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
    var copyOfExisting = (ObjectNode) existing.deepCopy();

    incoming.properties().forEach(entry -> {
      var incomingFieldName = entry.getKey();
      var incomingField = entry.getValue();
      var existingField = copyOfExisting.get(incomingFieldName);
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
    if (node.stringValue() != null && arrayNotContainsElement(array, node)) {
      array.add(node);
    }
  }

  private static boolean arrayNotContainsElement(ArrayNode array, JsonNode value) {
    return StreamSupport.stream(array.spliterator(), false)
      .filter(node -> node.stringValue() != null)
      .map(JsonNode::textValue)
      .noneMatch(text -> text.equals(value.stringValue()));
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
