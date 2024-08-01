package org.folio.linked.data.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.linked.data.test.TestUtil.loadResourceAsString;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;

@UnitTest
public class JsonUtilsTest {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  @Test
  public void merge_shouldReturnModifiedIncoming() throws JsonProcessingException {
    var existing = loadResourceAsString("samples/json_merge/full/existing.jsonl");
    var incoming = loadResourceAsString("samples/json_merge/full/incoming.jsonl");
    var merged = loadResourceAsString("samples/json_merge/full/merged.jsonl");
    JsonNode result = JsonUtils.merge(MAPPER.readTree(existing), MAPPER.readTree(incoming));
    assertThat(result.equals(MAPPER.readTree(merged))).isTrue();
  }
}
