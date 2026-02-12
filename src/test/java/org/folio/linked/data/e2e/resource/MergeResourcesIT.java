package org.folio.linked.data.e2e.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_LCCN;
import static org.folio.ld.dictionary.ResourceTypeDictionary.STATUS;
import static org.folio.linked.data.test.TestUtil.TENANT_ID;
import static org.folio.linked.data.test.TestUtil.TEST_JSON_MAPPER;
import static org.folio.linked.data.test.TestUtil.cleanResourceTables;
import static org.folio.linked.data.test.TestUtil.loadResourceAsString;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.ld.dictionary.PropertyDictionary;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.service.resource.graph.ResourceGraphService;
import org.folio.linked.data.service.tenant.TenantScopedExecutionService;
import org.folio.linked.data.test.MonographTestUtil;
import org.folio.linked.data.test.resource.ResourceTestService;
import org.folio.linked.data.util.JsonUtils;
import org.folio.spring.tools.kafka.KafkaAdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import tools.jackson.databind.JsonNode;

@IntegrationTest
class MergeResourcesIT {

  @Autowired
  private ResourceGraphService resourceGraphService;
  @Autowired
  private ResourceTestService resourceTestService;
  @Autowired
  private JdbcTemplate jdbcTemplate;
  @Autowired
  private TenantScopedExecutionService tenantScopedExecutionService;
  @MockitoSpyBean
  private KafkaAdminService kafkaAdminService;

  @BeforeEach
  void beforeEach() {
    tenantScopedExecutionService.execute(TENANT_ID, () -> {
        cleanResourceTables(jdbcTemplate);
        return null;
      }
    );
  }

  @Test
  void testResourcesMerging_1() {
    // given
    var graph1 = createGraph1toto2();
    var result1 = resourceGraphService.saveMergingGraph(graph1);
    assertThat(result1.rootResource().getId()).isEqualTo(1L);
    assertThat(result1.rootResource().isNew()).isFalse();
    assertThat(result1.newResources().stream().map(Resource::getId).toList()).contains(1L, 2L);
    assertThat(result1.newResources()).allMatch(r -> !r.isNew());
    assertThat(result1.updatedResources()).isEmpty();

    assertResourceConnectedToAnother(1L, 2L);
    var graph2 = createGraph3toto1to5toto4();

    // when
    var result2 = resourceGraphService.saveMergingGraph(graph2);
    assertThat(result2.rootResource().getId()).isEqualTo(3L);
    assertThat(result2.rootResource().isNew()).isFalse();
    assertThat(result2.newResources().stream().map(Resource::getId).toList()).contains(3L, 4L, 5L);
    assertThat(result2.newResources()).allMatch(r -> !r.isNew());
    assertThat(result2.updatedResources().stream().map(Resource::getId).toList()).contains(1L);
    assertThat(result2.updatedResources()).allMatch(r -> !r.isNew());

    // then
    // whole graph should be: 3 -> [(1 -> [2, 5]), 4]
    assertResourceConnectedToAnotherTwo(3L, 1L, 4L);
    assertResourceConnectedToAnotherTwo(1L, 2L, 5L);
  }

  @Test
  void testResourcesMerging_2() {
    // given
    var graph1 = createGraph3toto1to2to5toto4();
    resourceGraphService.saveMergingGraph(graph1);
    // whole graph should be: 3 -> [(1 -> [2, 5]), 4]
    assertResourceConnectedToAnotherTwo(3L, 1L, 4L);
    assertResourceConnectedToAnotherTwo(1L, 2L, 5L);
    var graph2 = createGraph6toto1toto4to5();

    // when
    resourceGraphService.saveMergingGraph(graph2);

    // then
    // whole graph should be: [3, 6] -> [(1 -> [2, 5]), (4 -> 5)]
    assertResourceConnectedToAnotherTwo(3L, 1L, 4L);
    assertResourceConnectedToAnotherTwo(1L, 2L, 5L);
    assertResourceConnectedToAnother(4L, 5L);
    assertResourceConnectedToAnotherTwo(6L, 1L, 4L);
  }

  @Test
  void testResourcesMerging1_and_2() {
    // given
    var graph1 = createGraph1toto2();
    resourceGraphService.saveMergingGraph(graph1);
    assertResourceConnectedToAnother(1L, 2L);
    var graph2 = createGraph3toto1to5toto4();

    // when
    resourceGraphService.saveMergingGraph(graph2);

    // then
    // whole graph should be: 3 -> [(1 -> [2, 5]), 4]
    assertResourceConnectedToAnotherTwo(3L, 1L, 4L);
    assertResourceConnectedToAnotherTwo(1L, 2L, 5L);
    assertResourceDoc("1", getInitialDoc());
    assertResourceDoc("4", getInitialDoc());

    // when
    var graph3 = createGraph6toto1toto4to5();
    resourceGraphService.saveMergingGraph(graph3);

    // then
    // whole graph should be: [3, 6] -> [(1 -> [2, 5]), (4 -> 5)]
    assertResourceConnectedToAnotherTwo(3L, 1L, 4L);
    assertResourceConnectedToAnotherTwo(1L, 2L, 5L);
    assertResourceConnectedToAnother(4L, 5L);
    assertResourceConnectedToAnotherTwo(6L, 1L, 4L);
    assertResourceDoc("1", getMergedDoc());
    assertResourceDoc("4", getMergedDoc());
  }

  @Test
  void should_remove_replacedBy_edge_when_resource_becomes_preferred() {
    // given
    var replacedByTargetResource = createResource(2L, Map.of()).setDoc(getInitialDoc());
    var lccnResource = new Resource().setIdAndRefreshEdges(3L).addTypes(ID_LCCN);
    var sourceResource = createResource(1L,
      Map.of(
        PredicateDictionary.REPLACED_BY, List.of(replacedByTargetResource),
        PredicateDictionary.MAP, List.of(lccnResource)
      )
    ).setDoc(getInitialDoc());
    resourceGraphService.saveMergingGraph(sourceResource);

    // when
    var statusResource = new Resource().setIdAndRefreshEdges(4L).addTypes(STATUS);
    var newSourceResource = createResource(1L,
      Map.of(PredicateDictionary.STATUS, List.of(statusResource))
    ).setDoc(getNewDoc());
    resourceGraphService.saveMergingGraph(newSourceResource);

    // then
    assertResourceDoc("1", getMergedDoc());
    var mergedResource = resourceTestService.getResourceById("1", 2);
    assertThat(mergedResource.getOutgoingEdges()).hasSize(2);
    assertThat(mergedResource.getOutgoingEdges())
      .anyMatch(edge -> edge.getPredicate().getUri().equals(PredicateDictionary.MAP.getUri()));
    assertThat(mergedResource.getOutgoingEdges())
      .anyMatch(edge -> edge.getPredicate().getUri().equals(PredicateDictionary.STATUS.getUri()));
    assertThat(mergedResource.getOutgoingEdges())
      .noneMatch(edge -> edge.getPredicate().getUri().equals(PredicateDictionary.REPLACED_BY.getUri()));
  }

  private void assertResourceConnectedToAnother(Long mainId, Long anotherId) {
    var mainResource = resourceTestService.getResourceById(mainId.toString(), 4);
    assertThat(mainResource.getOutgoingEdges()).hasSize(1);
    var edgeToAnother = mainResource.getOutgoingEdges().iterator().next();
    assertEdge(edgeToAnother, mainId, anotherId, mainResource);
  }

  private void assertResourceConnectedToAnotherTwo(Long mainId, Long firstConnectedId, Long secondConnectedId) {
    var mainResource = resourceTestService.getResourceById(mainId.toString(), 4);
    assertThat(mainResource.getOutgoingEdges()).hasSize(2);
    var mainEdgeIterator = mainResource.getOutgoingEdges().iterator();
    var firstEdge = mainEdgeIterator.next();
    assertEdge(firstEdge, mainId, firstConnectedId, mainResource);
    var secondEdge = mainEdgeIterator.next();
    assertEdge(secondEdge, mainId, secondConnectedId, mainResource);
  }

  private void assertResourceDoc(String id, JsonNode expected) {
    var resource = resourceTestService.getResourceById(id, 4);
    assertThat(resource.getDoc()).isEqualTo(expected);
  }

  private JsonNode getInitialDoc() {
    return getDocWithPreferredFlag("samples/json_merge/existing.jsonl", false);
  }

  private JsonNode getNewDoc() {
    return getDocWithPreferredFlag("samples/json_merge/incoming.jsonl", true);
  }

  private JsonNode getMergedDoc() {
    return getDocWithPreferredFlag("samples/json_merge/merged.jsonl", true);
  }

  private JsonNode getDocWithPreferredFlag(String docFile, boolean isPreferred) {
    var preferredJson = """
        {
          "http://library.link/vocab/resourcePreferred": [
            "$PREFERRED_FLAG"
          ]
        }
      """.replace("$PREFERRED_FLAG", String.valueOf(isPreferred));
    return JsonUtils.merge(TEST_JSON_MAPPER.readTree(loadResourceAsString(docFile)), TEST_JSON_MAPPER.readTree(preferredJson));
  }

  private Resource createGraph1toto2() {
    // 1 -> [2]
    var fp2Resource = createResource(2L, Map.of());
    return createResource(1L, Map.of(PredicateDictionary.ABRIDGER, List.of(fp2Resource))).setDoc(getInitialDoc());
  }

  private Resource createGraph3toto1to5toto4() {
    // 3 -> [(1 -> 5), 4]
    var fp5Resource = createResource(5L, Map.of());
    var fp1Resource = createResource(1L, Map.of(PredicateDictionary.BINDER, List.of(fp5Resource)))
      .setDoc(getInitialDoc());
    var fp4Resource = createResource(4L, Map.of()).setDoc(getInitialDoc()).setDoc(getInitialDoc());
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

  private Resource createGraph6toto1toto4to5() {
    // 6 -> [1, (4 -> 5)]
    var fp1Resource = createResource(1L, Map.of()).setDoc(getNewDoc());
    var fp5Resource = createResource(5L, Map.of());
    var fp4Resource = createResource(4L, Map.of(PredicateDictionary.FACSIMILIST, List.of(fp5Resource)))
      .setDoc(getNewDoc());
    return createResource(6L, Map.of(PredicateDictionary.GENRE, List.of(fp1Resource, fp4Resource)));
  }

  private Resource createResource(Long hash, Map<PredicateDictionary, List<Resource>> pred2OutgoingResources) {
    return MonographTestUtil.createResource(
      Map.of(PropertyDictionary.NAME, List.of("John Doe")),
      Set.of(ResourceTypeDictionary.IDENTIFIER),
      pred2OutgoingResources
    ).setIdAndRefreshEdges(hash);
  }

  private void assertEdge(ResourceEdge edge, long sourceHash, long targetHash, Resource source) {
    assertThat(edge.getId().getSourceHash()).isEqualTo(sourceHash);
    assertThat(edge.getId().getTargetHash()).isEqualTo(targetHash);
    assertThat(edge.getSource()).isEqualTo(source);
  }
}
