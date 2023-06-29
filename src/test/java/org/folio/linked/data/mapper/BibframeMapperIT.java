package org.folio.linked.data.mapper;

import static org.folio.linked.data.test.TestUtil.getResourceSample;
import static org.folio.linked.data.matcher.IsEqualJson.equalToJson;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
  void toBibframe_shouldReturnBibframeResponseForJsonNode() throws JsonProcessingException {
    // given
    var jsonNode = objectMapper.readTree(getResourceSample());

    // when
    var resource = bibframeMapper.map(jsonNode);

    // then
    assertThat(objectMapper.writeValueAsString(resource), equalToJson(getResourceSample()));
  }

  @Test
  void toBibframe_shouldReturnEmptyBibframeForNullInput() {
    // given
    JsonNode jsonNode = null;

    // when
    var bibframe = bibframeMapper.map(jsonNode);

    // then
    assertNull(bibframe.getWork());
    assertNull(bibframe.getInstance());
    assertNull(bibframe.getItem());
  }

}

