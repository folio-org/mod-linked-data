package org.folio.linked.data.e2e;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.ld.dictionary.PropertyDictionary;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.service.ResourceService;
import org.folio.linked.data.test.MonographTestUtil;
import org.folio.linked.data.utils.ResourceTestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.jdbc.JdbcTestUtils;

@IntegrationTest
class MergeResourcesIT {

  @Autowired
  private ResourceService resourceService;
  @Autowired
  private ResourceTestService resourceTestService;
  @Autowired
  private JdbcTemplate jdbcTemplate;

  @BeforeEach
  public void beforeEach() {
    JdbcTestUtils.deleteFromTables(jdbcTemplate, "resource_edges", "resource_type_map", "resources");
  }


  @Test
  void testResourcesMerging_1() {
    // given
    var graph1 = createGraph1__2();
    resourceService.saveMergingGraph(graph1);
    var fp1Resource = resourceTestService.getResourceById("1", 4);
    // Should be: 1 -> [2]
    assertThat(fp1Resource.getOutgoingEdges()).hasSize(1);
    var fpEdge1_2 = fp1Resource.getOutgoingEdges().iterator().next();
    assertThat(fpEdge1_2.getId().getSourceHash()).isEqualTo(1L);
    assertThat(fpEdge1_2.getId().getTargetHash()).isEqualTo(2L);
    assertThat(fpEdge1_2.getSource()).isEqualTo(fp1Resource);
    var graph2 = createGraph3__1_5__4();

    // when
    resourceService.saveMergingGraph(graph2);

    // then
    // whole graph should be: 3 -> [(1 -> [2, 5]), 4]
    // fp1Resource should be: 1 -> [2, 5]
    fp1Resource = resourceTestService.getResourceById("1", 4);
    assertThat(fp1Resource.getOutgoingEdges()).hasSize(2);
    var fp1Edgeiterator = fp1Resource.getOutgoingEdges().iterator();
    fpEdge1_2 = fp1Edgeiterator.next();
    assertThat(fpEdge1_2.getId().getSourceHash()).isEqualTo(1L);
    assertThat(fpEdge1_2.getId().getTargetHash()).isEqualTo(2L);
    assertThat(fpEdge1_2.getSource()).isEqualTo(fp1Resource);
    var fpEdge1_5 = fp1Edgeiterator.next();
    assertThat(fpEdge1_5.getId().getSourceHash()).isEqualTo(1L);
    assertThat(fpEdge1_5.getId().getTargetHash()).isEqualTo(5L);
    assertThat(fpEdge1_5.getSource()).isEqualTo(fp1Resource);
    // fp3Resource should be: 3 -> [1, 4]
    var fp3Resource = resourceTestService.getResourceById("3", 4);
    assertThat(fp3Resource.getOutgoingEdges()).hasSize(2);
    var fp3EdgeIterator = fp3Resource.getOutgoingEdges().iterator();
    var fpEdge3_1 = fp3EdgeIterator.next();
    assertThat(fpEdge3_1.getId().getSourceHash()).isEqualTo(3L);
    assertThat(fpEdge3_1.getId().getTargetHash()).isEqualTo(1L);
    assertThat(fpEdge3_1.getSource()).isEqualTo(fp3Resource);
    assertThat(fpEdge3_1.getTarget()).isEqualTo(fp1Resource);
    var fpEdge3_4 = fp3EdgeIterator.next();
    assertThat(fpEdge3_4.getId().getSourceHash()).isEqualTo(3L);
    assertThat(fpEdge3_4.getId().getTargetHash()).isEqualTo(4L);
    assertThat(fpEdge3_4.getSource()).isEqualTo(fp3Resource);
  }

  @Test
  void testResourcesMerging_2() {
    // given
    var graph1 = createGraph3__1_2_5__4();
    resourceService.saveMergingGraph(graph1);
    // fp3Resource should be: 3 -> [1, 4]
    var fp3Resource = resourceTestService.getResourceById("3", 4);
    assertThat(fp3Resource.getOutgoingEdges()).hasSize(2);
    var fp3EdgeIterator = fp3Resource.getOutgoingEdges().iterator();
    var fpEdge3_1 = fp3EdgeIterator.next();
    assertThat(fpEdge3_1.getId().getSourceHash()).isEqualTo(3L);
    assertThat(fpEdge3_1.getId().getTargetHash()).isEqualTo(1L);
    assertThat(fpEdge3_1.getSource()).isEqualTo(fp3Resource);
    var fpEdge3_4 = fp3EdgeIterator.next();
    assertThat(fpEdge3_4.getId().getSourceHash()).isEqualTo(3L);
    assertThat(fpEdge3_4.getId().getTargetHash()).isEqualTo(4L);
    assertThat(fpEdge3_4.getSource()).isEqualTo(fp3Resource);
    assertThat(fpEdge3_4.getTarget().getOutgoingEdges()).isEmpty();
    // fp1Resource should be: 3 -> 1 -> [2, 5]
    var fp1Resource = fpEdge3_1.getTarget();
    assertThat(fp1Resource.getOutgoingEdges()).hasSize(2);
    var fp1Edgeiterator = fp1Resource.getOutgoingEdges().iterator();
    var fpEdge1_2 = fp1Edgeiterator.next();
    assertThat(fpEdge1_2.getId().getSourceHash()).isEqualTo(1L);
    assertThat(fpEdge1_2.getId().getTargetHash()).isEqualTo(2L);
    assertThat(fpEdge1_2.getSource()).isEqualTo(fp1Resource);
    var fpEdge1_5 = fp1Edgeiterator.next();
    assertThat(fpEdge1_5.getId().getSourceHash()).isEqualTo(1L);
    assertThat(fpEdge1_5.getId().getTargetHash()).isEqualTo(5L);
    assertThat(fpEdge1_5.getSource()).isEqualTo(fp1Resource);
    var graph2 = createGraph6__1__4_5();

    // when
    resourceService.saveMergingGraph(graph2);

    // then
    // whole graph should be: 3 -> [(1 -> [2, 5]), 4 -> 5]
    // fp3Resource should be: 3 -> [1, 4]
    fp3Resource = resourceTestService.getResourceById("3", 10);
    assertThat(fp3Resource.getOutgoingEdges()).hasSize(2);
    fp3EdgeIterator = fp3Resource.getOutgoingEdges().iterator();
    fpEdge3_1 = fp3EdgeIterator.next();
    assertThat(fpEdge3_1.getId().getSourceHash()).isEqualTo(3L);
    assertThat(fpEdge3_1.getId().getTargetHash()).isEqualTo(1L);
    assertThat(fpEdge3_1.getSource()).isEqualTo(fp3Resource);
    fpEdge3_4 = fp3EdgeIterator.next();
    assertThat(fpEdge3_4.getId().getSourceHash()).isEqualTo(3L);
    assertThat(fpEdge3_4.getId().getTargetHash()).isEqualTo(4L);
    assertThat(fpEdge3_4.getSource()).isEqualTo(fp3Resource);
    // fp1Resource should be: 3 -> 1 -> [2, 5]
    fp1Resource = fpEdge3_1.getTarget();
    assertThat(fp1Resource.getOutgoingEdges()).hasSize(2);
    fp1Edgeiterator = fp1Resource.getOutgoingEdges().iterator();
    fpEdge1_2 = fp1Edgeiterator.next();
    assertThat(fpEdge1_2.getId().getSourceHash()).isEqualTo(1L);
    assertThat(fpEdge1_2.getId().getTargetHash()).isEqualTo(2L);
    assertThat(fpEdge1_2.getSource()).isEqualTo(fp1Resource);
    fpEdge1_5 = fp1Edgeiterator.next();
    assertThat(fpEdge1_5.getId().getSourceHash()).isEqualTo(1L);
    assertThat(fpEdge1_5.getId().getTargetHash()).isEqualTo(5L);
    assertThat(fpEdge1_5.getSource()).isEqualTo(fp1Resource);
    // fp4Resource should be: 3 -> 4 -> 5
    var fp4Resource = fpEdge3_4.getTarget();
    assertThat(fp4Resource.getOutgoingEdges()).hasSize(1);
    var fpEdge4_5 = fp4Resource.getOutgoingEdges().iterator().next();
    assertThat(fpEdge4_5.getId().getSourceHash()).isEqualTo(4L);
    assertThat(fpEdge4_5.getId().getTargetHash()).isEqualTo(5L);
    assertThat(fpEdge4_5.getSource()).isEqualTo(fp4Resource);

    // whole graph should be: 6 -> [(1 -> [2, 5]), 4 -> 5]
    // fp6Resource should be: 6 -> [1, 4]
    var fp6Resource = resourceTestService.getResourceById("6", 4);
    assertThat(fp6Resource.getOutgoingEdges()).hasSize(2);
    var fp6EdgeIterator = fp6Resource.getOutgoingEdges().iterator();
    var fp6_1Edge = fp6EdgeIterator.next();
    assertThat(fp6_1Edge.getId().getSourceHash()).isEqualTo(6L);
    assertThat(fp6_1Edge.getId().getTargetHash()).isEqualTo(1L);
    assertThat(fp6_1Edge.getSource()).isEqualTo(fp6Resource);
    // fp1Resource is already asserted
    assertThat(fp6_1Edge.getTarget()).isEqualTo(fp1Resource);
    var fp6_4Edge = fp6EdgeIterator.next();
    assertThat(fp6_4Edge.getId().getSourceHash()).isEqualTo(6L);
    assertThat(fp6_4Edge.getId().getTargetHash()).isEqualTo(4L);
    assertThat(fp6_4Edge.getSource()).isEqualTo(fp6Resource);
    // fp4Resource is already asserted
    assertThat(fp6_4Edge.getTarget()).isEqualTo(fp4Resource);
  }

  @Test
  void testResourcesMerging1_and_2() {
    // given
    var graph1 = createGraph1__2();
    resourceService.saveMergingGraph(graph1);
    var fp1Resource = resourceTestService.getResourceById("1", 4);
    // Should be: 1 -> [2]
    assertThat(fp1Resource.getOutgoingEdges()).hasSize(1);
    var fpEdge1_2 = fp1Resource.getOutgoingEdges().iterator().next();
    assertThat(fpEdge1_2.getId().getSourceHash()).isEqualTo(1L);
    assertThat(fpEdge1_2.getId().getTargetHash()).isEqualTo(2L);
    assertThat(fpEdge1_2.getSource()).isEqualTo(fp1Resource);
    var graph2 = createGraph3__1_5__4();

    // when
    resourceService.saveMergingGraph(graph2);

    // then
    fp1Resource = resourceTestService.getResourceById("1", 4);
    // fp1Resource should be: 1 -> [2, 5]
    assertThat(fp1Resource.getOutgoingEdges()).hasSize(2);
    var fp1Edgeiterator = fp1Resource.getOutgoingEdges().iterator();
    fpEdge1_2 = fp1Edgeiterator.next();
    assertThat(fpEdge1_2.getId().getSourceHash()).isEqualTo(1L);
    assertThat(fpEdge1_2.getId().getTargetHash()).isEqualTo(2L);
    assertThat(fpEdge1_2.getSource()).isEqualTo(fp1Resource);
    var fpEdge1_5 = fp1Edgeiterator.next();
    assertThat(fpEdge1_5.getId().getSourceHash()).isEqualTo(1L);
    assertThat(fpEdge1_5.getId().getTargetHash()).isEqualTo(5L);
    assertThat(fpEdge1_5.getSource()).isEqualTo(fp1Resource);
    // fp3Resource should be: 3 -> [(1 -> [2, 5]), 4]
    var fp3Resource = resourceTestService.getResourceById("3", 4);
    assertThat(fp3Resource.getOutgoingEdges()).hasSize(2);
    var fp3EdgeIterator = fp3Resource.getOutgoingEdges().iterator();
    var fpEdge3_1 = fp3EdgeIterator.next();
    assertThat(fpEdge3_1.getId().getSourceHash()).isEqualTo(3L);
    assertThat(fpEdge3_1.getId().getTargetHash()).isEqualTo(1L);
    assertThat(fpEdge3_1.getSource()).isEqualTo(fp3Resource);
    assertThat(fpEdge3_1.getTarget()).isEqualTo(fp1Resource);
    var fpEdge3_4 = fp3EdgeIterator.next();
    assertThat(fpEdge3_4.getId().getSourceHash()).isEqualTo(3L);
    assertThat(fpEdge3_4.getId().getTargetHash()).isEqualTo(4L);
    assertThat(fpEdge3_4.getSource()).isEqualTo(fp3Resource);

    // when
    var graph3 = createGraph6__1__4_5();
    resourceService.saveMergingGraph(graph3);

    // then
    // fp3Resource should be: 3 -> [1, 4]
    fp3Resource = resourceTestService.getResourceById("3", 10);
    assertThat(fp3Resource.getOutgoingEdges()).hasSize(2);
    fp3EdgeIterator = fp3Resource.getOutgoingEdges().iterator();
    fpEdge3_1 = fp3EdgeIterator.next();
    assertThat(fpEdge3_1.getId().getSourceHash()).isEqualTo(3L);
    assertThat(fpEdge3_1.getId().getTargetHash()).isEqualTo(1L);
    assertThat(fpEdge3_1.getSource()).isEqualTo(fp3Resource);
    fpEdge3_4 = fp3EdgeIterator.next();
    assertThat(fpEdge3_4.getId().getSourceHash()).isEqualTo(3L);
    assertThat(fpEdge3_4.getId().getTargetHash()).isEqualTo(4L);
    assertThat(fpEdge3_4.getSource()).isEqualTo(fp3Resource);
    // fp1Resource should be: 3 -> 1 -> [2, 5]
    fp1Resource = fpEdge3_1.getTarget();
    assertThat(fp1Resource.getOutgoingEdges()).hasSize(2);
    fp1Edgeiterator = fp1Resource.getOutgoingEdges().iterator();
    fpEdge1_2 = fp1Edgeiterator.next();
    assertThat(fpEdge1_2.getId().getSourceHash()).isEqualTo(1L);
    assertThat(fpEdge1_2.getId().getTargetHash()).isEqualTo(2L);
    assertThat(fpEdge1_2.getSource()).isEqualTo(fp1Resource);
    fpEdge1_5 = fp1Edgeiterator.next();
    assertThat(fpEdge1_5.getId().getSourceHash()).isEqualTo(1L);
    assertThat(fpEdge1_5.getId().getTargetHash()).isEqualTo(5L);
    assertThat(fpEdge1_5.getSource()).isEqualTo(fp1Resource);
    // fp4Resource should be: 3 -> 4 -> 5
    var fp4Resource = fpEdge3_4.getTarget();
    assertThat(fp4Resource.getOutgoingEdges()).hasSize(1);
    var fpEdge4_5 = fp4Resource.getOutgoingEdges().iterator().next();
    assertThat(fpEdge4_5.getId().getSourceHash()).isEqualTo(4L);
    assertThat(fpEdge4_5.getId().getTargetHash()).isEqualTo(5L);
    assertThat(fpEdge4_5.getSource()).isEqualTo(fp4Resource);

    // fp6Resource should be: 6 -> [1, 4]
    var fp6Resource = resourceTestService.getResourceById("6", 4);
    assertThat(fp6Resource.getOutgoingEdges()).hasSize(2);
    var fp6EdgeIterator = fp6Resource.getOutgoingEdges().iterator();
    var fp6_1Edge = fp6EdgeIterator.next();
    assertThat(fp6_1Edge.getId().getSourceHash()).isEqualTo(6L);
    assertThat(fp6_1Edge.getId().getTargetHash()).isEqualTo(1L);
    assertThat(fp6_1Edge.getSource()).isEqualTo(fp6Resource);
    assertThat(fp6_1Edge.getTarget()).isEqualTo(fp1Resource);
    var fp6_4Edge = fp6EdgeIterator.next();
    assertThat(fp6_4Edge.getId().getSourceHash()).isEqualTo(6L);
    assertThat(fp6_4Edge.getId().getTargetHash()).isEqualTo(4L);
    assertThat(fp6_4Edge.getSource()).isEqualTo(fp6Resource);
    assertThat(fp6_4Edge.getTarget()).isEqualTo(fp4Resource);
  }

  private Resource createGraph1__2() {
    // 1 -> [2]
    var fp2Resource = createResource(2L, Map.of());
    return createResource(1L, Map.of(PredicateDictionary.ABRIDGER, List.of(fp2Resource)));
  }

  private Resource createGraph3__1_5__4() {
    // 3 -> [(1 -> 5), 4]
    var fp5Resource = createResource(5L, Map.of());
    var fp1Resource = createResource(1L, Map.of(PredicateDictionary.BINDER, List.of(fp5Resource)));
    var fp4Resource = createResource(4L, Map.of());
    return createResource(3L, Map.of(PredicateDictionary.CREATOR, List.of(fp1Resource, fp4Resource)));
  }

  private Resource createGraph3__1_2_5__4() {
    // 3 -> [(1 -> 2, 5), 4]
    var fp2Resource = createResource(2L, Map.of());
    var fp5Resource = createResource(5L, Map.of());
    var fp1Resource = createResource(1L, Map.of(PredicateDictionary.DESIGNER, List.of(fp2Resource, fp5Resource)));
    var fp4Resource = createResource(4L, Map.of());
    return createResource(3L, Map.of(PredicateDictionary.EDITOR, List.of(fp1Resource, fp4Resource)));
  }

  private Resource createGraph6__1__4_5() {
    // 6 -> [1, (4 -> 5)]
    var fp1Resource = createResource(1L, Map.of());
    var fp5Resource = createResource(5L, Map.of());
    var fp4Resource = createResource(4L, Map.of(PredicateDictionary.FACSIMILIST, List.of(fp5Resource)));
    return createResource(6L, Map.of(PredicateDictionary.GENRE, List.of(fp1Resource, fp4Resource)));
  }

  private Resource createResource(Long hash, Map<PredicateDictionary, List<Resource>> pred2OutgoingResources) {
    return MonographTestUtil.createResource(
      Map.of(PropertyDictionary.NAME, List.of("John Doe")),
      Set.of(ResourceTypeDictionary.IDENTIFIER),
      pred2OutgoingResources
    ).setResourceHash(hash);
  }
}
