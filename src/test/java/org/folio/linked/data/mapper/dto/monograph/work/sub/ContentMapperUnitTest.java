package org.folio.linked.data.mapper.dto.monograph.work.sub;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.PredicateDictionary.IS_DEFINED_BY;
import static org.folio.ld.dictionary.PropertyDictionary.CODE;
import static org.folio.ld.dictionary.PropertyDictionary.LABEL;
import static org.folio.ld.dictionary.PropertyDictionary.LINK;
import static org.folio.ld.dictionary.PropertyDictionary.TERM;
import static org.folio.ld.dictionary.ResourceTypeDictionary.CATEGORY_SET;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.folio.linked.data.configuration.ErrorResponseConfig;
import org.folio.linked.data.domain.dto.Category;
import org.folio.linked.data.exception.RequestProcessingExceptionBuilder;
import org.folio.linked.data.mapper.dto.common.CoreMapperImpl;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceTypeEntity;
import org.folio.linked.data.service.resource.hash.HashService;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

@SpringBootTest(classes = ContentMapperUnit.class)
@Import({CoreMapperImpl.class, ObjectMapper.class, RequestProcessingExceptionBuilder.class, ErrorResponseConfig.class})
@UnitTest
class ContentMapperUnitTest {
  @Autowired
  private ContentMapperUnit contentMapperUnit;
  @Autowired
  private ObjectMapper objectMapper;
  @MockBean
  private HashService hashService;

  @Test
  void toEntity_shouldDeriveMarcCodeFromLink() {
    // given
    var expectedMarcCode = "crd";
    var expectedLink = "http://id.loc.gov/vocabulary/contentTypes/" + expectedMarcCode;
    var expectedTerm = expectedMarcCode + "_term";

    Category category = new Category()
      .link(List.of(expectedLink))
      .term(List.of(expectedTerm));

    // when
    var resource = contentMapperUnit.toEntity(category, new Resource());

    // then
    Map<String, List<String>> props = objectMapper.convertValue(resource.getDoc(), new TypeReference<>() {
    });
    assertThat(props.get(LINK.getValue())).hasSize(1).contains(expectedLink);
    assertThat(props.get(TERM.getValue())).hasSize(1).contains(expectedTerm);
    assertThat(props.get(CODE.getValue())).hasSize(1).contains(expectedMarcCode);
  }

  @Test
  void toEntity_shouldCreateCategorySet() {
    // when
    var resource = contentMapperUnit.toEntity(new Category(), new Resource());

    // then
    assertThat(resource.getOutgoingEdges()).hasSize(1);

    var outgoingEdge = resource.getOutgoingEdges().stream().findFirst().get();
    assertThat(outgoingEdge.getPredicate().getUri()).isEqualTo(IS_DEFINED_BY.getUri());

    var target = outgoingEdge.getTarget();
    assertThat(target.getTypes())
      .extracting(ResourceTypeEntity::getUri)
      .containsExactly(CATEGORY_SET.getUri());

    Map<String, List<String>> targetProps = objectMapper.convertValue(target.getDoc(), new TypeReference<>() {
    });
    assertThat(targetProps.get(LINK.getValue()))
      .hasSize(1)
      .contains("http://id.loc.gov/vocabulary/genreFormSchemes/rdacontent");
    assertThat(targetProps.get(LABEL.getValue()))
      .hasSize(1)
      .contains("rdacontent");
  }
}
