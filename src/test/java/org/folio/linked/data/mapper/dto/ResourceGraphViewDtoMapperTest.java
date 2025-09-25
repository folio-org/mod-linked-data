package org.folio.linked.data.mapper.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.linked.data.test.TestUtil.OBJECT_MAPPER;

import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@UnitTest
class ResourceGraphViewDtoMapperTest {

  private ResourceGraphViewDtoMapper mapper;

  @BeforeEach
  void setUp() {
    mapper = new ResourceGraphViewDtoMapper(OBJECT_MAPPER);
  }

  @Test
  void fromJson_validJson_returnsDto() {
    var json = """
      {
        "id": "123",
        "label": "test label",
        "doc": {"key1": ["value1"]},
        "types": ["type1", "type2"],
        "outgoingEdges": {
          "edge1": [
            {
              "id": "nested1",
              "label": "nested label",
              "doc": {"nestedKey": ["nestedValue"]},
              "types": ["nestedType"],
              "outgoingEdges": {}
            }
          ]
        }
      }""";
    var result = mapper.fromJson(json);
    assertThat(result).isPresent();
    var dto = result.get();
    assertThat(dto.getId()).isEqualTo("123");
    assertThat(dto.getLabel()).isEqualTo("test label");
    assertThat(dto.getDoc()).containsEntry("key1", java.util.List.of("value1"));
    assertThat(dto.getTypes()).containsExactly("type1", "type2");
    assertThat(dto.getOutgoingEdges()).containsKey("edge1");
    var edge1List = dto.getOutgoingEdges().get("edge1");
    assertThat(edge1List).hasSize(1);
    var nested = edge1List.getFirst();
    assertThat(nested.getId()).isEqualTo("nested1");
    assertThat(nested.getLabel()).isEqualTo("nested label");
    assertThat(nested.getDoc()).containsEntry("nestedKey", java.util.List.of("nestedValue"));
    assertThat(nested.getTypes()).containsExactly("nestedType");
    assertThat(nested.getOutgoingEdges()).isEmpty();
  }

  @Test
  void fromJson_invalidJson_returnsEmpty() {
    var json = "{invalid json}";
    var result = mapper.fromJson(json);
    assertThat(result).isEmpty();
  }
}
