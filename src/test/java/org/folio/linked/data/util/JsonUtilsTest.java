package org.folio.linked.data.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.linked.data.test.TestUtil.loadResourceAsString;
import static org.folio.linked.data.util.JsonUtils.merge;
import static org.folio.linked.data.util.JsonUtils.writeValueAsString;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
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
}
