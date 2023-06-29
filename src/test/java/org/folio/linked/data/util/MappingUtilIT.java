package org.folio.linked.data.util;

import static org.folio.linked.data.test.TestUtil.getResourceSample;
import static org.folio.linked.data.matcher.IsEqualJson.equalToJson;
import static org.folio.linked.data.util.MappingUtil.toJson;
import static org.hamcrest.MatcherAssert.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
class MappingUtilIT {

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void toJson_shouldReturnCorrectJsonNodeFromString() throws JsonProcessingException {
    // given
    var json = getResourceSample();

    // when
    var jsonNode = toJson(json, objectMapper);

    // then
    assertThat(objectMapper.writeValueAsString(jsonNode), equalToJson(getResourceSample()));
  }

  @Test
  void toJson_shouldReturnCorrectJsonNodeFromMap() throws JsonProcessingException {
    // given
    var json = getResourceSample();
    var map = objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {
    });

    // when
    var jsonNode = toJson(map, objectMapper);

    // then
    assertThat(objectMapper.writeValueAsString(jsonNode), equalToJson(getResourceSample()));
  }


  @Test
  void toJson_shouldReturnEmptyJsonNodeForNullInput() throws JsonProcessingException {
    // given
    Object configuration = null;

    // when
    var jsonNode = toJson(configuration, objectMapper);

    // then
    assertThat(objectMapper.writeValueAsString(jsonNode), equalToJson("{}"));
  }
}
