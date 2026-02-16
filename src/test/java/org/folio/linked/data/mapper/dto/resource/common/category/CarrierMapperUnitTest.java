package org.folio.linked.data.mapper.dto.resource.common.category;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.PropertyDictionary.CODE;
import static org.folio.ld.dictionary.PropertyDictionary.LINK;
import static org.folio.ld.dictionary.PropertyDictionary.TERM;
import static org.folio.linked.data.test.TestUtil.TEST_JSON_MAPPER;

import java.util.List;
import java.util.Map;
import org.folio.linked.data.configuration.ErrorResponseConfig;
import org.folio.linked.data.domain.dto.Category;
import org.folio.linked.data.exception.RequestProcessingExceptionBuilder;
import org.folio.linked.data.mapper.dto.resource.base.CoreMapperImpl;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.service.resource.hash.HashService;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import tools.jackson.core.type.TypeReference;

@SpringBootTest(classes = CarrierMapperUnit.class)
@Import({CoreMapperImpl.class, RequestProcessingExceptionBuilder.class, ErrorResponseConfig.class})
@UnitTest
class CarrierMapperUnitTest {
  @Autowired
  private CarrierMapperUnit carrierMapperUnit;
  @MockitoBean
  private HashService hashService;

  @Test
  void toEntity_shouldDeriveMarcCodeFromLink() {
    // given
    var expectedMarcCode = "ha";
    var expectedLink = "http://id.loc.gov/vocabulary/carriers/" + expectedMarcCode;
    var expectedTerm = expectedMarcCode + "_term";

    Category category = new Category()
      .link(List.of(expectedLink))
      .term(List.of(expectedTerm));

    // when
    var resource = carrierMapperUnit.toEntity(category, new Resource());

    // then
    Map<String, List<String>> props = TEST_JSON_MAPPER.convertValue(resource.getDoc(), new TypeReference<>() {
    });
    assertThat(props.get(LINK.getValue())).hasSize(1).contains(expectedLink);
    assertThat(props.get(TERM.getValue())).hasSize(1).contains(expectedTerm);
    assertThat(props.get(CODE.getValue())).hasSize(1).contains(expectedMarcCode);
  }
}
