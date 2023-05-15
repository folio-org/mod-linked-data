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
    var jsonNode = bibframeMapper.toJson(getBibframeSample());
    assertThat(OBJECT_MAPPER.writeValueAsString(jsonNode), equalToJson(getBibframeSample()));
  }

  @Test
  void toJson_shouldReturnCorrectJsonNodeFromMap() throws JsonProcessingException {
    var map = OBJECT_MAPPER.readValue(getBibframeSample(),
        new TypeReference<Map<String, Object>>() {});
    var jsonNode = bibframeMapper.toJson(map);
    assertThat(OBJECT_MAPPER.writeValueAsString(jsonNode), equalToJson(getBibframeSample()));
  }

  @Test
  void toJson_shouldReturnEmptyJsonNodeForNullInput() throws JsonProcessingException {
    var jsonNode = bibframeMapper.toJson(null);
    assertThat(OBJECT_MAPPER.writeValueAsString(jsonNode), equalToJson("{}"));
  }

}

