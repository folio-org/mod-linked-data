package org.folio.linked.data.mapper;

import static org.folio.linked.data.TestUtil.OBJECT_MAPPER;
import static org.folio.linked.data.TestUtil.getBibframeSample;
import static org.folio.linked.data.matcher.IsEqualJson.equalToJson;
import static org.hamcrest.MatcherAssert.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.Map;
import org.folio.spring.test.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

@UnitTest
class BibframeMapperTest {

  private BibframeMapper bibframeMapper = Mappers.getMapper(BibframeMapper.class);

  @Test
  void toJson_shouldReturnCorrectJsonNodeFromString() throws JsonProcessingException {
    // given
    var json = getBibframeSample();

    // when
    var jsonNode = bibframeMapper.toJson(json);

    // then
    assertThat(OBJECT_MAPPER.writeValueAsString(jsonNode), equalToJson(getBibframeSample()));
  }

  @Test
  void toJson_shouldReturnCorrectJsonNodeFromMap() throws JsonProcessingException {
    // given
    var json = getBibframeSample();
    var map = OBJECT_MAPPER.readValue(json, new TypeReference<Map<String, Object>>() {});

    // when
    var jsonNode = bibframeMapper.toJson(map);

    // then
    assertThat(OBJECT_MAPPER.writeValueAsString(jsonNode), equalToJson(getBibframeSample()));
  }

  @Test
  void toJson_shouldReturnEmptyJsonNodeForNullInput() throws JsonProcessingException {
    // given
    Object configuration = null;

    // when
    var jsonNode = bibframeMapper.toJson(configuration);

    // then
    assertThat(OBJECT_MAPPER.writeValueAsString(jsonNode), equalToJson("{}"));
  }

}

