package org.folio.linked.data.mapper;

import static org.folio.linked.data.TestUtil.getBibframeSample;
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
class BibframeMapperIT {

  @Autowired
  private BibframeMapper bibframeMapper;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void toJson_shouldReturnCorrectJsonNodeFromString() throws JsonProcessingException {
    // given
    var json = getBibframeSample();

    // when
    var jsonNode = bibframeMapper.toJson(json);

    // then
    assertThat(objectMapper.writeValueAsString(jsonNode), equalToJson(getBibframeSample()));
  }

  @Test
  void toJson_shouldReturnCorrectJsonNodeFromMap() throws JsonProcessingException {
    // given
    var json = getBibframeSample();
    var map = objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {
    });

    // when
    var jsonNode = bibframeMapper.toJson(map);

    // then
    assertThat(objectMapper.writeValueAsString(jsonNode), equalToJson(getBibframeSample()));
  }

  @Test
  void toJson_shouldReturnEmptyJsonNodeForNullInput() throws JsonProcessingException {
    // given
    Object configuration = null;

    // when
    var jsonNode = bibframeMapper.toJson(configuration);

    // then
    assertThat(objectMapper.writeValueAsString(jsonNode), equalToJson("{}"));
  }

  @Test
  void toBibframe_shouldReturnBibframeResponseForJsonNode() throws JsonProcessingException {
    // given
    var jsonNode = objectMapper.readTree(getBibframeSample());

    // when
    var bibframe = bibframeMapper.toBibframe(jsonNode);

    // then
    assertThat(objectMapper.writeValueAsString(bibframe), equalToJson(getBibframeSample()));
  }

  @Test
  void toBibframe_shouldReturnEmptyBibframeForNullInput() throws JsonProcessingException {
    // given
    JsonNode jsonNode = null;

    // when
    var bibframe = bibframeMapper.toBibframe(jsonNode);

    // then
    assertNull(bibframe.getWork());
    assertNull(bibframe.getInstance());
    assertNull(bibframe.getItem());
  }

}

