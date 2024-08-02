package org.folio.linked.data.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.linked.data.test.TestUtil.loadResourceAsString;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;

@UnitTest
class JsonUtilsTest {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  @Test
  void merge_shouldReturnModifiedIncoming() throws JsonProcessingException {
    var existing = loadResourceAsString("samples/json_merge/existing.jsonl");
    var incoming = loadResourceAsString("samples/json_merge/incoming.jsonl");
    var merged = loadResourceAsString("samples/json_merge/merged.jsonl");
    var result = JsonUtils.merge(MAPPER.readTree(existing), MAPPER.readTree(incoming));
    assertThat(result.equals(MAPPER.readTree(merged))).isTrue();
  }
}
