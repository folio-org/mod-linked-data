package org.folio.linked.data.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;

@UnitTest
class ResourceModelMapperTest {

  private final ResourceModelMapper mapper = new ResourceModelMapperImpl();

  @Test
  void toModel_depthZero_shouldNotMapOutgoingEdges() {
    // given
    var root = buildThreeNodeGraph();

    // when
    var model = mapper.toModel(root, 0);

    // then
    assertThat(model.getOutgoingEdges()).isEmpty();
  }

  @Test
  void toModel_depthOne_shouldMapOnlyFirstLevelOutgoingEdges() {
    // given
    var root = buildThreeNodeGraph();

    // when
    var model = mapper.toModel(root, 1);

    // then
    assertThat(model.getOutgoingEdges()).hasSize(1);
    var child = model.getOutgoingEdges().iterator().next().getTarget();
    assertThat(child.getOutgoingEdges()).isEmpty();
  }

  @Test
  void toModel_depthTwo_shouldMapTwoLevelsOfOutgoingEdges() {
    // given
    var root = buildThreeNodeGraph();

    // when
    var model = mapper.toModel(root, 2);

    // then
    assertThat(model.getOutgoingEdges()).hasSize(1);
    var child = model.getOutgoingEdges().iterator().next().getTarget();
    assertThat(child.getOutgoingEdges()).hasSize(1);
    var grandChild = child.getOutgoingEdges().iterator().next().getTarget();
    assertThat(grandChild.getOutgoingEdges()).isEmpty();
  }

  private static Resource buildThreeNodeGraph() {
    var root = resource(1L);
    var child = resource(2L);
    var grandChild = resource(3L);

    root.addOutgoingEdge(new ResourceEdge(root, child, PredicateDictionary.TITLE));
    child.addOutgoingEdge(new ResourceEdge(child, grandChild, PredicateDictionary.TITLE));

    return root;
  }

  private static Resource resource(Long id) {
    return new Resource()
      .setIdAndRefreshEdges(id)
      .addTypes(ResourceTypeDictionary.INSTANCE);
  }
}
