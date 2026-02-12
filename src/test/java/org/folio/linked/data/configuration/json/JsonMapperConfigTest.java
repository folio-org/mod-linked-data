package org.folio.linked.data.configuration.json;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.linked.data.test.TestUtil.loadResourceAsString;
import static org.folio.linked.data.util.JsonUtils.JSON_MAPPER;

import lombok.Data;
import org.folio.linked.data.domain.dto.ImportOutputEvent;
import org.folio.linked.data.domain.dto.MarcRecord;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;

@UnitTest
class JsonMapperConfigTest {

  @Test
  void serializationStringFieldMarkedAsRaw_shouldReturnOriginalString() {
    // given
    var jsonField = """
      {"raw": "some value"}""";
    var markRecord = new MarcRecord();
    markRecord.setContent(jsonField);
    var expectedJsonString = """
      {"content":{"raw": "some value"}}""";

    // when
    var jsonString = JSON_MAPPER.writeValueAsString(markRecord);

    // then
    assertThat(jsonString)
      .isEqualTo(expectedJsonString);
  }

  @Test
  void serializationStringFieldNotMarkedAsRaw_shouldReturnFormattedString() {
    // given
    var jsonField = """
      {"raw": "some value"}""";
    var notMarcRecord = new TestClass();
    notMarcRecord.setContent(jsonField);
    var expectedJsonString = """
      {"content":"{\\"raw\\": \\"some value\\"}"}""";

    // when
    var jsonString = JSON_MAPPER.writeValueAsString(notMarcRecord);

    // then
    assertThat(jsonString)
      .isEqualTo(expectedJsonString);
  }

  @Data
  private static class TestClass {
    private String content;
  }

  @Test
  void importOutputEventDeserialization() {
    // given
    var value = loadResourceAsString("samples/importOutputEvent.json");

    // when
    var result = JSON_MAPPER.readValue(value, ImportOutputEvent.class);

    // then
    assertThat(result.getTs()).isEqualTo("1762182290977");
    assertThat(result.getTenant()).isEqualTo("test_tenant");
    assertThat(result.getResourcesWithLineNumbers()).hasSize(1);
    var resourceWithLineNumber = result.getResourcesWithLineNumbers().iterator().next();
    assertThat(resourceWithLineNumber.getResource().getTypes()).contains(INSTANCE);
    assertThat(resourceWithLineNumber.getResource().getOutgoingEdges()).hasSize(2);
  }

}
