package org.folio.linked.data.configuration.json;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.folio.linked.data.domain.dto.MarkRecord;
import org.folio.spring.test.type.UnitTest;
import org.junit.jupiter.api.Test;

@UnitTest
class ObjectMapperConfigTest {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapperConfig().objectMapper();

  @Test
  void serializationStringFieldMarkedAsRaw_shouldReturnOriginalString() throws JsonProcessingException {
    // given
    var jsonField = """
      {"raw": "some value"}""";
    var markRecord = new MarkRecord();
    markRecord.setContent(jsonField);
    var expectedJsonString = """
      {"content":{"raw": "some value"}}""";

    // when
    var jsonString = OBJECT_MAPPER.writeValueAsString(markRecord);

    // then
    assertThat(jsonString)
      .isEqualTo(expectedJsonString);
  }

  @Test
  void serializationStringFieldNotMarkedAsRaw_shouldReturnFormattedString() throws JsonProcessingException {
    // given
    var jsonField = """
      {"raw": "some value"}""";
    var notMarcRecord = new TestClass();
    notMarcRecord.setContent(jsonField);
    var expectedJsonString = """
      {"content":"{\\"raw\\": \\"some value\\"}"}""";

    // when
    var jsonString = OBJECT_MAPPER.writeValueAsString(notMarcRecord);

    // then
    assertThat(jsonString)
      .isEqualTo(expectedJsonString);
  }

  @Data
  private static class TestClass {
    private String content;
  }
}
