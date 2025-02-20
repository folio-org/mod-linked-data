package org.folio.linked.data.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.linked.data.test.TestUtil.loadResourceAsString;
import static org.folio.linked.data.util.JsonUtils.merge;
import static org.folio.linked.data.util.JsonUtils.writeValueAsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@UnitTest
class JsonUtilsTest {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  private static Stream<Arguments> jsonObjects() {
    return Stream.of(
      Arguments.of("{\"key\":\"value\"}", "{\"key\":\"value\"}"),
      Arguments.of(Map.of("key", "value"), "{\"key\":\"value\"}"));
  }

  @ParameterizedTest
  @MethodSource("jsonObjects")
  void writeValueAsString_shouldReturnCorrectString(Object obj, String expected) {
    assertThat(writeValueAsString(obj, MAPPER)).isEqualTo(expected);
  }

  @Test
  void merge_shouldReturnModifiedIncoming() throws JsonProcessingException {
    var existing = loadResourceAsString("samples/json_merge/existing.jsonl");
    var incoming = loadResourceAsString("samples/json_merge/incoming.jsonl");
    var merged = loadResourceAsString("samples/json_merge/merged.jsonl");
    var result = merge(MAPPER.readTree(existing), MAPPER.readTree(incoming));
    assertThat(result).isEqualTo(MAPPER.readTree(merged));
  }


  @ParameterizedTest
  @MethodSource("hasElementByJsonPathArguments")
  void hasElementByJsonPath_shouldReturnCorrectBoolean(String jsonPath, boolean expected) {
    // when
    var actual = JsonUtils.hasElementByJsonPath(LINKED_DATA_ID_JSON, jsonPath);

    // then
    assertEquals(expected, actual);
  }

  @Test
  void testGetProperty() {
    // given
    ObjectNode node = JsonNodeFactory.instance.objectNode();
    node.put("testProperty", "testValue");

    // when
    Optional<JsonNode> result = JsonUtils.getProperty(node, "testProperty");

    // then
    assertTrue(result.isPresent());
    assertThat(result.get().asText()).isEqualTo("testValue");
  }

  @Test
  void testGetProperty_NotFound() {
    // given
    ObjectNode node = JsonNodeFactory.instance.objectNode();

    // when
    Optional<JsonNode> result = JsonUtils.getProperty(node, "nonExistentProperty");

    // then
    assertTrue(result.isEmpty());
  }

  @Test
  void testSetProperty() {
    // given
    ObjectNode node = JsonNodeFactory.instance.objectNode();
    JsonNode value = JsonNodeFactory.instance.textNode("newValue");

    // when
    JsonUtils.setProperty(node, "newProperty", value);

    // then
    assertThat(node.get("newProperty").asText()).isEqualTo("newValue");
  }

  @Test
  void testSetProperty_Overwrite() {
    // given
    ObjectNode node = JsonNodeFactory.instance.objectNode();
    node.put("existingProperty", "oldValue");
    JsonNode value = JsonNodeFactory.instance.textNode("newValue");

    // when
    JsonUtils.setProperty(node, "existingProperty", value);

    // then
    assertThat(node.get("existingProperty").asText()).isEqualTo("newValue");
  }

  static Stream<Arguments> hasElementByJsonPathArguments() {
    return Stream.of(
      Arguments.of("$.fields[*].999.subfields[*].l", true),
      Arguments.of("$.fields[*].999.subfields[*].a", false),
      Arguments.of("$.fields[*].111", false),
      Arguments.of(null, false)
    );
  }

  private static final String LINKED_DATA_ID_JSON = """
        {
          "fields": [
            {
              "999": {
                "subfields": [
                  {
                    "l": "lvalue"
                  }
                ],
                "ind1": " ",
                "ind2": " "
              }
            }
          ]
        }
        """;
}
