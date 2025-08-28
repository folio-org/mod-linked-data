package org.folio.linked.data.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.PredicateDictionary.IS_DEFINED_BY;
import static org.folio.linked.data.test.TestUtil.readTree;

import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;

@UnitTest
class ResourceModelMapperTest {

  private final ResourceModelMapper resourceModelMapper = new ResourceModelMapperImpl();

  @Test
  void shouldMapResourceAndOutgoingEdgesToModel() {

    var categoryDoc = readTree("""
      {
        "http://bibfra.me/vocab/marc/term": "category1",
        "http://bibfra.me/vocab/marc/code": "c1"
      }""");
    var categoryEntity = new Resource()
      .addTypes(ResourceTypeDictionary.CATEGORY)
      .setDoc(categoryDoc)
      .setLabel("category1");

    var categorySetDoc = readTree("""
      {
        "http://bibfra.me/vocab/marc/term": "categorySet1",
        "http://bibfra.me/vocab/marc/code": "cs1",
        "http://bibfra.me/vocab/lite/link": "http://example.org/categorySet1"
      }""");
    var categorySetEntity = new Resource()
      .addTypes(ResourceTypeDictionary.CATEGORY_SET)
      .setDoc(categorySetDoc)
      .setLabel("categorySet1");

    var edge = new ResourceEdge(categoryEntity, categorySetEntity, IS_DEFINED_BY);
    categoryEntity.addOutgoingEdge(edge);
    categorySetEntity.addIncomingEdge(edge);

    var categoryModel = resourceModelMapper.toModel(categoryEntity);
    assertThat(categoryModel.getTypes()).containsExactly(ResourceTypeDictionary.CATEGORY);
    assertThat(categoryModel.getDoc()).isEqualTo(categoryDoc);

    var categorySetModelOpt = categoryModel.getOutgoingEdges()
      .stream()
      .filter(e -> e.getPredicate() == IS_DEFINED_BY)
      .map(org.folio.ld.dictionary.model.ResourceEdge::getTarget)
      .findFirst();
    assertThat(categorySetModelOpt).isPresent();
    assertThat(categorySetModelOpt.get().getTypes()).containsExactly(ResourceTypeDictionary.CATEGORY_SET);
    assertThat(categorySetModelOpt.get().getDoc()).isEqualTo(categorySetDoc);
  }
}
