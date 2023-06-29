package org.folio.linked.data.util;

import static org.folio.linked.data.matcher.IsEqualJson.equalToJson;
import static org.folio.linked.data.test.TestUtil.getResourceSample;
import static org.hamcrest.MatcherAssert.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.linked.data.mapper.resource.common.CommonMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
class CommonMapperIT {

  @Autowired
  private CommonMapper commonMapper;
  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void toJson_shouldReturnCorrectJsonNodeFromString() throws JsonProcessingException {
    // given
    var json = getResourceSample();

    // when
    var jsonNode = commonMapper.toJson(json);

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
    var jsonNode = commonMapper.toJson(map);

    // then
    assertThat(objectMapper.writeValueAsString(jsonNode), equalToJson(getResourceSample()));
  }


  @Test
  void toJson_shouldReturnEmptyJsonNodeForNullInput() throws JsonProcessingException {
    // given
    Object configuration = null;

    // when
    var jsonNode = commonMapper.toJson(configuration);

    // then
    assertThat(objectMapper.writeValueAsString(jsonNode), equalToJson("{}"));
  }
}
