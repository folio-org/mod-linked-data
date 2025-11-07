package org.folio.linked.data.mapper.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.TITLE;

import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@UnitTest
class ResourceSubgraphViewMapperTest {

  private ResourceSubgraphViewMapper mapper;

  @BeforeEach
  void setUp() {
    mapper = new ResourceSubgraphViewMapper();
  }

  @Test
  void fromJson_validJson_returnsDto() {
    var json = """
      {
         "id": "123",
         "label": "test label",
         "types": [
           "http://bibfra.me/vocab/lite/Instance"
         ],
         "doc": {
           "key1": [
             "value1"
           ]
         },
         "outgoingEdges": {
           "http://bibfra.me/vocab/library/title": [
             {
               "id": "456",
               "doc": {
                 "nestedKey": [
                   "nestedValue"
                 ]
               },
               "label": "nested label",
               "types": [
                 "http://bibfra.me/vocab/library/Title"
               ],
               "outgoingEdges": {}
             }
           ]
         }
       }""";
    var result = mapper.fromJson(json);
    assertThat(result).isPresent();
    var resource = result.get();
    assertThat(resource.getId()).isEqualTo(123L);
    assertThat(resource.getLabel()).isEqualTo("test label");
    assertThat(resource.getDoc().get("key1").get(0).asText()).isEqualTo("value1");
    assertThat(resource.getTypes()).containsExactly(INSTANCE);
    assertThat(resource.getOutgoingEdges()).hasSize(1);
    var edge1 = resource.getOutgoingEdges().iterator().next();
    assertThat(edge1.getSource()).isEqualTo(resource);
    assertThat(edge1.getPredicate()).isEqualTo(PredicateDictionary.TITLE);
    var nested = edge1.getTarget();
    assertThat(nested.getId()).isEqualTo(456L);
    assertThat(nested.getLabel()).isEqualTo("nested label");
    assertThat(nested.getDoc().get("nestedKey").get(0).asText()).isEqualTo("nestedValue");
    assertThat(nested.getTypes()).containsExactly(TITLE);
    assertThat(nested.getOutgoingEdges()).isEmpty();
  }

  @Test
  void fromJson_invalidJson_returnsEmpty() {
    var json = "{invalid json}";
    var result = mapper.fromJson(json);
    assertThat(result).isEmpty();
  }
}
