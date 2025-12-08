package org.folio.linked.data.configuration.json;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.linked.data.test.TestUtil.EMPTY_EXCEPTION_BUILDER;
import static org.folio.linked.data.test.TestUtil.loadResourceAsString;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.folio.linked.data.domain.dto.ImportOutputEvent;
import org.folio.linked.data.domain.dto.MarcRecord;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;

@UnitTest
class ObjectMapperConfigTest {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapperConfig().objectMapper(EMPTY_EXCEPTION_BUILDER);

  @Test
  void serializationStringFieldMarkedAsRaw_shouldReturnOriginalString() throws JsonProcessingException {
    // given
    var jsonField = """
      {"raw": "some value"}""";
    var markRecord = new MarcRecord();
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

  @Test
  void importOutputEventDeserialization() throws JsonProcessingException {
    // given
    var value = loadResourceAsString("samples/importOutputEvent.json");

    // when
    var result = OBJECT_MAPPER.readValue(value, ImportOutputEvent.class);

    // then
    assertThat(result.getTs()).isEqualTo("1762182290977");
    assertThat(result.getTenant()).isEqualTo("test_tenant");
    assertThat(result.getResourcesWithLineNumbers()).hasSize(1);
    var resourceWithLineNumber = result.getResourcesWithLineNumbers().iterator().next();
    assertThat(resourceWithLineNumber.getResource().getTypes()).contains(INSTANCE);
    assertThat(resourceWithLineNumber.getResource().getOutgoingEdges()).hasSize(2);
  }

}
