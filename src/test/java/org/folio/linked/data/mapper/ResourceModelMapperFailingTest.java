package org.folio.linked.data.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.PredicateDictionary.IS_DEFINED_BY;
import static org.folio.linked.data.test.TestUtil.readTree;

import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
class ResourceModelMapperFailingTest {

  @Autowired
  private ResourceModelMapper resourceModelMapper;

  @Test
  void failingTest() {

    var categoryResourceWithNoId = new Resource()
      .addTypes(ResourceTypeDictionary.CATEGORY)
      .setDoc(readTree("""
        {
          "http://bibfra.me/vocab/marc/term": "category1"
        }"""))
      .setLabel("category1");

    var categorySetResourceWithNoId = new Resource()
      .addTypes(ResourceTypeDictionary.CATEGORY_SET)
      .setDoc(readTree("""
        {
          "http://bibfra.me/vocab/marc/term": "categorySet1"
        }"""))
      .setLabel("categorySet1");

    var edge = new ResourceEdge(categoryResourceWithNoId, categorySetResourceWithNoId, IS_DEFINED_BY);
    categoryResourceWithNoId.addOutgoingEdge(edge);
    categorySetResourceWithNoId.addIncomingEdge(edge);


    var result = resourceModelMapper.toModel(categoryResourceWithNoId);

    assertThat(result.getTypes()).contains(ResourceTypeDictionary.CATEGORY);

    var firstOutgoingResource = result.getOutgoingEdges().iterator().next().getTarget();
    /*
    FAILURE IN THE NEXT LINE
    If you put a breakpoint here, you can see that 'result' and 'firstOutgoingResource' are the same object.,
    */
    assertThat(firstOutgoingResource).isNotEqualTo(result);
  }

  @Test
  void passingTestWhenResourcesHaveId() {

    var categoryResourceWithNoId = new Resource()
      .addTypes(ResourceTypeDictionary.CATEGORY)
      .setDoc(readTree("""
        {
          "http://bibfra.me/vocab/marc/term": "category1"
        }"""))
      .setLabel("category1")
      .setId(1L);

    var categorySetResourceWithNoId = new Resource()
      .addTypes(ResourceTypeDictionary.CATEGORY_SET)
      .setDoc(readTree("""
        {
          "http://bibfra.me/vocab/marc/term": "categorySet1"
        }"""))
      .setLabel("categorySet1")
      .setId(2L);

    var edge = new ResourceEdge(categoryResourceWithNoId, categorySetResourceWithNoId, IS_DEFINED_BY);
    categoryResourceWithNoId.addOutgoingEdge(edge);
    categorySetResourceWithNoId.addIncomingEdge(edge);


    var result = resourceModelMapper.toModel(categoryResourceWithNoId);

    assertThat(result.getTypes()).contains(ResourceTypeDictionary.CATEGORY);

    var firstOutgoingResource = result.getOutgoingEdges().iterator().next().getTarget();
    assertThat(firstOutgoingResource).isNotEqualTo(result);
  }

}
