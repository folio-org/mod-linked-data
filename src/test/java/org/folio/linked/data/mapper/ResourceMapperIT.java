package org.folio.linked.data.mapper;

import static org.folio.linked.data.TestUtil.getResourceSample;
import static org.folio.linked.data.matcher.IsEqualJson.equalToJson;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
class ResourceMapperIT {

  @Autowired
  private ResourceMapper resourceMapper;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void toJson_shouldReturnCorrectJsonNodeFromString() throws JsonProcessingException {
    // given
    var json = getResourceSample();

    // when
    var jsonNode = resourceMapper.toJson(json);

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
    var jsonNode = resourceMapper.toJson(map);

    // then
    assertThat(objectMapper.writeValueAsString(jsonNode), equalToJson(getResourceSample()));
  }



  @Test
  void toJson_shouldReturnEmptyJsonNodeForNullInput() throws JsonProcessingException {
    // given
    Object configuration = null;

    // when
    var jsonNode = resourceMapper.toJson(configuration);

    // then
    assertThat(objectMapper.writeValueAsString(jsonNode), equalToJson("{}"));
  }

  @Test
  void toBibframe_shouldReturnBibframeResponseForJsonNode() throws JsonProcessingException {
    // given
    var jsonNode = objectMapper.readTree(getResourceSample());

    // when
    var resource = resourceMapper.map(jsonNode);

    // then
    assertThat(objectMapper.writeValueAsString(resource), equalToJson(getResourceSample()));
  }

  @Test
  void toBibframe_shouldReturnEmptyBibframeForNullInput() {
    // given
    JsonNode jsonNode = null;

    // when
    var bibframe = resourceMapper.map(jsonNode);

    // then
    assertNull(bibframe.getWork());
    assertNull(bibframe.getInstance());
    assertNull(bibframe.getItem());
  }

}

