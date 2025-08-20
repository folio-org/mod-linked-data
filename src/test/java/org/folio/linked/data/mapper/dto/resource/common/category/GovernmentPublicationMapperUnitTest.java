package org.folio.linked.data.mapper.dto.resource.common.category;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.PropertyDictionary.CODE;
import static org.folio.ld.dictionary.PropertyDictionary.LINK;
import static org.folio.ld.dictionary.PropertyDictionary.TERM;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.folio.linked.data.configuration.ErrorResponseConfig;
import org.folio.linked.data.domain.dto.Category;
import org.folio.linked.data.exception.RequestProcessingExceptionBuilder;
import org.folio.linked.data.mapper.dto.resource.base.CoreMapperImpl;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.service.resource.hash.HashService;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(classes = GovernmentPublicationMapperUnit.class)
@Import({CoreMapperImpl.class, ObjectMapper.class, RequestProcessingExceptionBuilder.class, ErrorResponseConfig.class})
@UnitTest
class GovernmentPublicationMapperUnitTest {
  @Autowired
  private GovernmentPublicationMapperUnit governmentPublicationMapperUnit;
  @Autowired
  private ObjectMapper objectMapper;
  @MockitoBean
  private HashService hashService;

  @ParameterizedTest
  @CsvSource({
    "g, o",
    "x, x"
  })
  void toEntity_shouldDeriveMarcCodeFromLink(String linkSuffix, String expectedMarcCode) {
    // given
    var expectedLink = "http://id.loc.gov/vocabulary/mgovtpubtype/" + linkSuffix;
    var expectedTerm = linkSuffix + "_term";

    Category category = new Category()
      .link(List.of(expectedLink))
      .term(List.of(expectedTerm));

    // when
    var resource = governmentPublicationMapperUnit.toEntity(category, new Resource());

    // then
    Map<String, List<String>> props = objectMapper.convertValue(resource.getDoc(), new TypeReference<>() {
    });
    assertThat(props.get(LINK.getValue())).hasSize(1).contains(expectedLink);
    assertThat(props.get(TERM.getValue())).hasSize(1).contains(expectedTerm);
    assertThat(props.get(CODE.getValue())).hasSize(1).contains(expectedMarcCode);
  }
}
