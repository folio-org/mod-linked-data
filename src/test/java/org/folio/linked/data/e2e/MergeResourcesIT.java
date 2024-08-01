package org.folio.linked.data.e2e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.linked.data.test.TestUtil.loadResourceAsString;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.SneakyThrows;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.ld.dictionary.PropertyDictionary;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.service.ResourceService;
import org.folio.linked.data.test.MonographTestUtil;
import org.folio.linked.data.test.ResourceTestService;
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
  @Autowired
  private ObjectMapper objectMapper;

  @BeforeEach
  public void beforeEach() {
    JdbcTestUtils.deleteFromTables(jdbcTemplate, "resource_edges", "resource_type_map", "resources");
  }

  @Test
  void testResourcesMerging_1() {
    // given
    var graph1 = createGraph1toto2();
    resourceService.saveMergingGraph(graph1);
    var fp1Resource = resourceTestService.getResourceById("1", 4);
    // Should be: 1 -> [2]
    assertThat(fp1Resource.getOutgoingEdges()).hasSize(1);
    var fpEdge1to2 = fp1Resource.getOutgoingEdges().iterator().next();
    assertEdge(fpEdge1to2, 1L, 2L, fp1Resource);
    var graph2 = createGraph3toto1to5toto4();

    // when
    resourceService.saveMergingGraph(graph2);

    // then
    // whole graph should be: 3 -> [(1 -> [2, 5]), 4]
    // fp1Resource should be: 1 -> [2, 5]
    fp1Resource = resourceTestService.getResourceById("1", 4);
    assertThat(fp1Resource.getOutgoingEdges()).hasSize(2);
    var fp1Edgeiterator = fp1Resource.getOutgoingEdges().iterator();
    fpEdge1to2 = fp1Edgeiterator.next();
    assertEdge(fpEdge1to2, 1L, 2L, fp1Resource);
    var fpEdge1to5 = fp1Edgeiterator.next();
    assertEdge(fpEdge1to5, 1L, 5L, fp1Resource);
    // fp3Resource should be: 3 -> [1, 4]
    var fp3Resource = resourceTestService.getResourceById("3", 4);
    assertThat(fp3Resource.getOutgoingEdges()).hasSize(2);
    var fp3EdgeIterator = fp3Resource.getOutgoingEdges().iterator();
    var fpEdge3to1 = fp3EdgeIterator.next();
    assertEdge(fpEdge3to1, 3L, 1L, fp3Resource);
    assertThat(fpEdge3to1.getTarget()).isEqualTo(fp1Resource);
    var fpEdge3to4 = fp3EdgeIterator.next();
    assertEdge(fpEdge3to4, 3L, 4L, fp3Resource);
  }

  @Test
  void testResourcesMerging_2() {
    // given
    var graph1 = createGraph3toto1to2to5toto4();
    resourceService.saveMergingGraph(graph1);
    // fp3Resource should be: 3 -> [1, 4]
    var fp3Resource = resourceTestService.getResourceById("3", 4);
    assertThat(fp3Resource.getOutgoingEdges()).hasSize(2);
    var fp3EdgeIterator = fp3Resource.getOutgoingEdges().iterator();
    var fpEdge3to1 = fp3EdgeIterator.next();
    assertEdge(fpEdge3to1, 3L, 1L, fp3Resource);
    var fpEdge3to4 = fp3EdgeIterator.next();
    assertEdge(fpEdge3to4, 3L, 4L, fp3Resource);
    assertThat(fpEdge3to4.getTarget().getOutgoingEdges()).isEmpty();
    // fp1Resource should be: 3 -> 1 -> [2, 5]
    var fp1Resource = fpEdge3to1.getTarget();
    assertThat(fp1Resource.getOutgoingEdges()).hasSize(2);
    var fp1Edgeiterator = fp1Resource.getOutgoingEdges().iterator();
    var fpEdge1to2 = fp1Edgeiterator.next();
    assertEdge(fpEdge1to2, 1L, 2L, fp1Resource);
    var fpEdge1to5 = fp1Edgeiterator.next();
    assertEdge(fpEdge1to5, 1L, 5L, fp1Resource);
    var graph2 = createGraph6toto1toto4to5();

    // when
    resourceService.saveMergingGraph(graph2);

    // then
    // whole graph should be: 3 -> [(1 -> [2, 5]), 4 -> 5]
    // fp3Resource should be: 3 -> [1, 4]
    fp3Resource = resourceTestService.getResourceById("3", 10);
    assertThat(fp3Resource.getOutgoingEdges()).hasSize(2);
    fp3EdgeIterator = fp3Resource.getOutgoingEdges().iterator();
    fpEdge3to1 = fp3EdgeIterator.next();
    assertEdge(fpEdge3to1, 3L, 1L, fp3Resource);
    fpEdge3to4 = fp3EdgeIterator.next();
    assertEdge(fpEdge3to4, 3L, 4L, fp3Resource);
    // fp1Resource should be: 3 -> 1 -> [2, 5]
    fp1Resource = fpEdge3to1.getTarget();
    assertThat(fp1Resource.getOutgoingEdges()).hasSize(2);
    fp1Edgeiterator = fp1Resource.getOutgoingEdges().iterator();
    fpEdge1to2 = fp1Edgeiterator.next();
    assertEdge(fpEdge1to2, 1L, 2L, fp1Resource);
    fpEdge1to5 = fp1Edgeiterator.next();
    assertEdge(fpEdge1to5, 1L, 5L, fp1Resource);
    // fp4Resource should be: 3 -> 4 -> 5
    var fp4Resource = fpEdge3to4.getTarget();
    assertThat(fp4Resource.getOutgoingEdges()).hasSize(1);
    var fpEdge4to5 = fp4Resource.getOutgoingEdges().iterator().next();
    assertEdge(fpEdge4to5, 4L, 5L, fp4Resource);

    // whole graph should be: 6 -> [(1 -> [2, 5]), 4 -> 5]
    // fp6Resource should be: 6 -> [1, 4]
    var fp6Resource = resourceTestService.getResourceById("6", 4);
    assertThat(fp6Resource.getOutgoingEdges()).hasSize(2);
    var fp6EdgeIterator = fp6Resource.getOutgoingEdges().iterator();
    var fp6to1Edge = fp6EdgeIterator.next();
    assertEdge(fp6to1Edge, 6L, 1L, fp6Resource);
    // fp1Resource is already asserted
    assertThat(fp6to1Edge.getTarget()).isEqualTo(fp1Resource);
    var fp6to4Edge = fp6EdgeIterator.next();
    assertEdge(fp6to4Edge, 6L, 4L, fp6Resource);
    // fp4Resource is already asserted
    assertThat(fp6to4Edge.getTarget()).isEqualTo(fp4Resource);
  }

  @Test
  void testResourcesMerging1_and_2() {
    // given
    var graph1 = createGraph1toto2();
    resourceService.saveMergingGraph(graph1);
    var fp1Resource = resourceTestService.getResourceById("1", 4);
    // Should be: 1 -> [2]
    assertThat(fp1Resource.getOutgoingEdges()).hasSize(1);
    var fpEdge1to2 = fp1Resource.getOutgoingEdges().iterator().next();
    assertEdge(fpEdge1to2, 1L, 2L, fp1Resource);
    var graph2 = createGraph3toto1to5toto4();

    // when
    resourceService.saveMergingGraph(graph2);

    // then
    fp1Resource = resourceTestService.getResourceById("1", 4);
    // fp1Resource should be: 1 -> [2, 5]
    assertThat(fp1Resource.getOutgoingEdges()).hasSize(2);
    var fp1Edgeiterator = fp1Resource.getOutgoingEdges().iterator();
    fpEdge1to2 = fp1Edgeiterator.next();
    assertEdge(fpEdge1to2, 1L, 2L, fp1Resource);
    var fpEdge1to5 = fp1Edgeiterator.next();
    assertEdge(fpEdge1to5, 1L, 5L, fp1Resource);
    // fp3Resource should be: 3 -> [(1 -> [2, 5]), 4]
    var fp3Resource = resourceTestService.getResourceById("3", 4);
    assertThat(fp3Resource.getOutgoingEdges()).hasSize(2);
    var fp3EdgeIterator = fp3Resource.getOutgoingEdges().iterator();
    var fpEdge3to1 = fp3EdgeIterator.next();
    assertEdge(fpEdge3to1, 3L, 1L, fp3Resource);
    assertThat(fpEdge3to1.getTarget()).isEqualTo(fp1Resource);
    var fpEdge3to4 = fp3EdgeIterator.next();
    assertEdge(fpEdge3to4, 3L, 4L, fp3Resource);
    var fp4Resource = resourceTestService.getResourceById("4", 4);
    assertThat(fp1Resource.getDoc().equals(getInitialDoc(1L))).isTrue();
    assertThat(fp4Resource.getDoc().equals(getInitialDoc(4L))).isTrue();

    // when
    var graph3 = createGraph6toto1toto4to5();
    resourceService.saveMergingGraph(graph3);

    // then
    // fp3Resource should be: 3 -> [1, 4]
    fp3Resource = resourceTestService.getResourceById("3", 10);
    assertThat(fp3Resource.getOutgoingEdges()).hasSize(2);
    fp3EdgeIterator = fp3Resource.getOutgoingEdges().iterator();
    fpEdge3to1 = fp3EdgeIterator.next();
    assertEdge(fpEdge3to1, 3L, 1L, fp3Resource);
    fpEdge3to4 = fp3EdgeIterator.next();
    assertEdge(fpEdge3to4, 3L, 4L, fp3Resource);
    // fp1Resource should be: 3 -> 1 -> [2, 5]
    fp1Resource = fpEdge3to1.getTarget();
    assertThat(fp1Resource.getOutgoingEdges()).hasSize(2);
    fp1Edgeiterator = fp1Resource.getOutgoingEdges().iterator();
    fpEdge1to2 = fp1Edgeiterator.next();
    assertEdge(fpEdge1to2, 1L, 2L, fp1Resource);
    fpEdge1to5 = fp1Edgeiterator.next();
    assertEdge(fpEdge1to5, 1L, 5L, fp1Resource);
    // fp4Resource should be: 3 -> 4 -> 5
    fp4Resource = fpEdge3to4.getTarget();
    assertThat(fp4Resource.getOutgoingEdges()).hasSize(1);
    var fpEdge4to5 = fp4Resource.getOutgoingEdges().iterator().next();
    assertEdge(fpEdge4to5, 4L, 5L, fp4Resource);

    // fp6Resource should be: 6 -> [1, 4]
    var fp6Resource = resourceTestService.getResourceById("6", 4);
    assertThat(fp6Resource.getOutgoingEdges()).hasSize(2);
    var fp6EdgeIterator = fp6Resource.getOutgoingEdges().iterator();
    var fp6to1Edge = fp6EdgeIterator.next();
    assertEdge(fp6to1Edge, 6L, 1L, fp6Resource);
    assertThat(fp6to1Edge.getTarget()).isEqualTo(fp1Resource);
    var fp6to4Edge = fp6EdgeIterator.next();
    assertEdge(fp6to4Edge, 6L, 4L, fp6Resource);
    assertThat(fp6to4Edge.getTarget()).isEqualTo(fp4Resource);
    assertThat(fp1Resource.getDoc().equals(getMergedDoc(1L))).isTrue();
    assertThat(fp4Resource.getDoc().equals(getMergedDoc(4L))).isTrue();
  }

  @SneakyThrows
  private JsonNode getInitialDoc(Long id) {
    return getDoc("samples/json_merge/basic/id-%s/old.jsonl", id);
  }

  @SneakyThrows
  private JsonNode getNewDoc(Long id) {
    return getDoc("samples/json_merge/basic/id-%s/new.jsonl", id);
  }

  @SneakyThrows
  private JsonNode getMergedDoc(Long id) {
    return getDoc("samples/json_merge/basic/id-%s/merged.jsonl", id);
  }

  @SneakyThrows
  private JsonNode getDoc(String template, Long id) {
    return objectMapper.readTree(loadResourceAsString(String.format(template, id)));
  }

  private Resource createGraph1toto2() {
    // 1 -> [2]
    var fp2Resource = createResource(2L, Map.of());
    return createResource(1L, Map.of(PredicateDictionary.ABRIDGER, List.of(fp2Resource)));
  }

  private Resource createGraph3toto1to5toto4() {
    // 3 -> [(1 -> 5), 4]
    var fp5Resource = createResource(5L, Map.of());
    var fp1Resource = createResource(1L, Map.of(PredicateDictionary.BINDER, List.of(fp5Resource)))
      .setDoc(getInitialDoc(1L));
    var fp4Resource = createResource(4L, Map.of()).setDoc(getInitialDoc(4L));
    return createResource(3L, Map.of(PredicateDictionary.CREATOR, List.of(fp1Resource, fp4Resource)));
  }

  private Resource createGraph3toto1to2to5toto4() {
    // 3 -> [(1 -> 2, 5), 4]
    var fp2Resource = createResource(2L, Map.of());
    var fp5Resource = createResource(5L, Map.of());
    var fp1Resource = createResource(1L, Map.of(PredicateDictionary.DESIGNER, List.of(fp2Resource, fp5Resource)));
    var fp4Resource = createResource(4L, Map.of());
    return createResource(3L, Map.of(PredicateDictionary.EDITOR, List.of(fp1Resource, fp4Resource)));
  }

  @SneakyThrows
  private Resource createGraph6toto1toto4to5() {
    // 6 -> [1, (4 -> 5)]
    var fp1Resource = createResource(1L, Map.of()).setDoc(getNewDoc(1L));
    var fp5Resource = createResource(5L, Map.of());
    var fp4Resource = createResource(4L, Map.of(PredicateDictionary.FACSIMILIST, List.of(fp5Resource)))
      .setDoc(getNewDoc(4L));
    return createResource(6L, Map.of(PredicateDictionary.GENRE, List.of(fp1Resource, fp4Resource)));
  }

  private Resource createResource(Long hash, Map<PredicateDictionary, List<Resource>> pred2OutgoingResources) {
    return MonographTestUtil.createResource(
      Map.of(PropertyDictionary.NAME, List.of("John Doe")),
      Set.of(ResourceTypeDictionary.IDENTIFIER),
      pred2OutgoingResources
    ).setId(hash);
  }

  private void assertEdge(ResourceEdge edge, long sourceHash, long targetHash, Resource source) {
    assertThat(edge.getId().getSourceHash()).isEqualTo(sourceHash);
    assertThat(edge.getId().getTargetHash()).isEqualTo(targetHash);
    assertThat(edge.getSource()).isEqualTo(source);
  }
}
