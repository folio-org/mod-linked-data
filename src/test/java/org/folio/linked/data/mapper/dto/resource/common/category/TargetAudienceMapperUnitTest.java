package org.folio.linked.data.mapper.dto.resource.common.category;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.PredicateDictionary.IS_DEFINED_BY;
import static org.folio.ld.dictionary.PropertyDictionary.CODE;
import static org.folio.ld.dictionary.PropertyDictionary.LABEL;
import static org.folio.ld.dictionary.PropertyDictionary.LINK;
import static org.folio.ld.dictionary.PropertyDictionary.TERM;
import static org.folio.ld.dictionary.ResourceTypeDictionary.CATEGORY_SET;
import static org.folio.linked.data.test.TestUtil.TEST_JSON_MAPPER;

import java.util.List;
import java.util.Map;
import org.folio.linked.data.configuration.ErrorResponseConfig;
import org.folio.linked.data.domain.dto.Category;
import org.folio.linked.data.exception.RequestProcessingExceptionBuilder;
import org.folio.linked.data.mapper.dto.resource.base.CoreMapperImpl;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceTypeEntity;
import org.folio.linked.data.service.resource.hash.HashService;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import tools.jackson.core.type.TypeReference;

@SpringBootTest(classes = TargetAudienceMapperUnit.class)
@Import({CoreMapperImpl.class, RequestProcessingExceptionBuilder.class, ErrorResponseConfig.class})
@UnitTest
class TargetAudienceMapperUnitTest {
  @Autowired
  private TargetAudienceMapperUnit targetAudienceMapperUnit;
  @MockitoBean
  private HashService hashService;

  @ParameterizedTest
  @CsvSource({
    "pre, a",
    "pri, b",
    "pad, c",
    "ado, d",
    "adu, e",
    "spe, f",
    "gen, g",
    "juv, j"
  })
  void toEntity_shouldDeriveMarcCodeFromLink(String linkSuffix, String expectedMarcCode) {
    // given
    var expectedLink = "http://id.loc.gov/vocabulary/maudience/" + linkSuffix;
    var expectedTerm = linkSuffix + "_term";

    Category category = new Category()
      .link(List.of(expectedLink))
      .term(List.of(expectedTerm));

    // when
    var resource = targetAudienceMapperUnit.toEntity(category, new Resource());

    // then
    Map<String, List<String>> props = TEST_JSON_MAPPER.convertValue(resource.getDoc(), new TypeReference<>() {
    });
    assertThat(props.get(LINK.getValue())).hasSize(1).contains(expectedLink);
    assertThat(props.get(TERM.getValue())).hasSize(1).contains(expectedTerm);
    assertThat(props.get(CODE.getValue())).hasSize(1).contains(expectedMarcCode);
  }

  @Test
  void toEntity_shouldCreateCategorySet() {
    // when
    var resource = targetAudienceMapperUnit.toEntity(new Category(), new Resource());

    // then
    assertThat(resource.getOutgoingEdges()).hasSize(1);

    var outgoingEdge = resource.getOutgoingEdges().stream().findFirst().get();
    assertThat(outgoingEdge.getPredicate().getUri()).isEqualTo(IS_DEFINED_BY.getUri());

    var target = outgoingEdge.getTarget();
    assertThat(target.getTypes())
      .extracting(ResourceTypeEntity::getUri)
      .containsExactly(CATEGORY_SET.getUri());

    Map<String, List<String>> targetProps = TEST_JSON_MAPPER.convertValue(target.getDoc(), new TypeReference<>() {
    });
    assertThat(targetProps.get(LINK.getValue()))
      .hasSize(1)
      .contains("http://id.loc.gov/vocabulary/maudience");
    assertThat(targetProps.get(LABEL.getValue()))
      .hasSize(1)
      .contains("Target audience");
  }
}
