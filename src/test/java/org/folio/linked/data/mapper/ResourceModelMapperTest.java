package org.folio.linked.data.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@UnitTest
class ResourceModelMapperTest {

  private final ResourceModelMapper mapper = new ResourceModelMapperImpl();

  @ParameterizedTest
  @CsvSource({
    "0, 0, 0",
    "1, 1, 0",
    "2, 1, 1"
  })
  void toModel_shouldMapEdgesUpToRequestedDepth(int depth, int expectedLevel1Size, int expectedLevel2Size) {
    // given
    var root = getResourcesGraph();

    // when
    var model = mapper.toModel(root, depth);

    // then
    assertThat(model.getOutgoingEdges()).hasSize(expectedLevel1Size);
    assertThat(model.getIncomingEdges()).hasSize(expectedLevel1Size);
    if (expectedLevel1Size > 0) {
      assertThat(model.getOutgoingEdges().iterator().next().getTarget().getOutgoingEdges()).hasSize(expectedLevel2Size);
      assertThat(model.getIncomingEdges().iterator().next().getSource().getIncomingEdges()).hasSize(expectedLevel2Size);
    }
  }

  @Test
  void toModel_depthOne_outgoingAndIncomingDepthsAreIndependent() {
    // given
    var root = getResourcesGraph();

    // when
    var model = mapper.toModel(root, 1);

    // then
    assertThat(model.getOutgoingEdges()).hasSize(1);
    assertThat(model.getIncomingEdges()).hasSize(1);

    var mappedChild = model.getOutgoingEdges().iterator().next().getTarget();
    assertThat(mappedChild.getOutgoingEdges()).isEmpty();
    assertThat(mappedChild.getIncomingEdges()).hasSize(1);

    var mappedParent = model.getIncomingEdges().iterator().next().getSource();
    assertThat(mappedParent.getIncomingEdges()).isEmpty();
    assertThat(mappedParent.getOutgoingEdges()).hasSize(1);
  }

  @Test
  void toModel_depthGreaterThanMax_shouldThrow() {
    // given
    var root = buildThreeNodeOutgoingGraph();

    // when / then
    assertThatThrownBy(() -> mapper.toModel(root, 8))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("Requested edges depth is too high: 8. Maximum allowed is 7");
  }

  private Resource getResourcesGraph() {
    var grandParent = resource(5L);
    var parent = resource(4L);
    var root = resource(1L);
    linkOutgoing(grandParent, parent);
    linkOutgoing(parent, root);
    var child = resource(2L);
    var grandChild = resource(3L);
    linkOutgoing(root, child);
    linkOutgoing(child, grandChild);
    return root;
  }

  private Resource buildThreeNodeOutgoingGraph() {
    var root = resource(1L);
    var child = resource(2L);
    var grandChild = resource(3L);
    linkOutgoing(root, child);
    linkOutgoing(child, grandChild);
    return root;
  }

  private void linkOutgoing(Resource source, Resource target) {
    var edge = new ResourceEdge(source, target, PredicateDictionary.TITLE);
    source.addOutgoingEdge(edge);
    target.addIncomingEdge(edge);
  }

  private static Resource resource(Long id) {
    return new Resource()
      .setIdAndRefreshEdges(id)
      .addTypes(ResourceTypeDictionary.INSTANCE);
  }
}

