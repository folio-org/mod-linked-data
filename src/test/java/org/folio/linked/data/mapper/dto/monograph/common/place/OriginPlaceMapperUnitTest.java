package org.folio.linked.data.mapper.dto.monograph.common.place;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.PropertyDictionary.CODE;
import static org.folio.ld.dictionary.PropertyDictionary.LINK;
import static org.folio.ld.dictionary.PropertyDictionary.NAME;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.folio.linked.data.domain.dto.Place;
import org.folio.linked.data.mapper.dto.common.CoreMapperImpl;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.service.resource.hash.HashService;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

@SpringBootTest(classes = OriginPlaceMapperUnit.class)
@Import({CoreMapperImpl.class, ObjectMapper.class})
@UnitTest
class OriginPlaceMapperUnitTest {

  @Autowired
  private OriginPlaceMapperUnit originPlaceMapperUnit;
  @Autowired
  private ObjectMapper objectMapper;
  @MockBean
  private HashService hashService;

  @Test
  void toEntity_shouldDeriveMarcCodeFromLink() {
    //given
    var expectedMarcCode = "fr";
    var expectedLink = "http://id.loc.gov/vocabulary/countries/" + expectedMarcCode;
    var expectedName = "France";

    var place = new Place()
      .link(List.of(expectedLink))
      .name(List.of(expectedName));

    //when
    var resource = originPlaceMapperUnit.toEntity(place, new Resource());

    //then
    var props = objectMapper.convertValue(resource.getDoc(), new TypeReference<Map<String, List<String>>>() {});
    assertThat(props.get(CODE.getValue())).hasSize(1).contains(expectedMarcCode);
    assertThat(props.get(LINK.getValue())).hasSize(1).contains(expectedLink);
    assertThat(props.get(NAME.getValue())).hasSize(1).contains(expectedName);
  }
}
