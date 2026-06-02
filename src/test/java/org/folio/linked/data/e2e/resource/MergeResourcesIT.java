package org.folio.linked.data.e2e.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.PropertyDictionary.ATTRIBUTION;
import static org.folio.ld.dictionary.PropertyDictionary.AUTHORITY_LINK;
import static org.folio.ld.dictionary.PropertyDictionary.CONTROL_FIELD;
import static org.folio.ld.dictionary.PropertyDictionary.EQUIVALENT;
import static org.folio.ld.dictionary.PropertyDictionary.FIELD_LINK;
import static org.folio.ld.dictionary.PropertyDictionary.LINKAGE;
import static org.folio.ld.dictionary.PropertyDictionary.NAME_ALTERNATIVE;
import static org.folio.ld.dictionary.PropertyDictionary.PLACE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.CONCEPT;
import static org.folio.ld.dictionary.ResourceTypeDictionary.FORM;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_LCCN;
import static org.folio.ld.dictionary.ResourceTypeDictionary.MEETING;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ORGANIZATION;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PERSON;
import static org.folio.ld.dictionary.ResourceTypeDictionary.STATUS;
import static org.folio.ld.dictionary.ResourceTypeDictionary.TOPIC;
import static org.folio.linked.data.test.TestUtil.TENANT_ID;
import static org.folio.linked.data.test.TestUtil.TEST_JSON_MAPPER;
import static org.folio.linked.data.test.TestUtil.cleanResourceTables;
import static org.folio.linked.data.test.TestUtil.loadResourceAsString;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.StreamSupport;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.ld.dictionary.PropertyDictionary;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.linked.data.model.entity.FolioMetadata;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.service.resource.graph.ResourceGraphService;
import org.folio.linked.data.service.tenant.TenantScopedExecutionService;
import org.folio.linked.data.test.MonographTestUtil;
import org.folio.linked.data.test.resource.ResourceTestService;
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
  void shouldRemoveReplacedByEdge_whenResourceGetsFolioMetadata() {
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
    newSourceResource.setFolioMetadata(new FolioMetadata(newSourceResource));
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

  @Test
  void shouldMergePerson100MultiValuedSubfields() {
    // given
    resourceGraphService.saveMergingGraph(createPersonResource(1L, Map.of(
      ATTRIBUTION, List.of("attribution-v1"),
      NAME_ALTERNATIVE, List.of("name-alt-v1")
    )));

    // when
    resourceGraphService.saveMergingGraph(createPersonResource(1L, Map.of(
      ATTRIBUTION, List.of("attribution-v2"),
      NAME_ALTERNATIVE, List.of("name-alt-v2")
    )));

    // then
    var doc = resourceTestService.getResourceById("1", 1).getDoc();
    assertDocValues(doc, ATTRIBUTION, List.of("attribution-v1", "attribution-v2"));
    assertDocValues(doc, NAME_ALTERNATIVE, List.of("name-alt-v1", "name-alt-v2"));
  }

  @Test
  void shouldMergeOrganization110MultiValuedSubfields() {
    // given
    resourceGraphService.saveMergingGraph(createOrganizationResource(1L, Map.of(
      PLACE, List.of("place-v1"),
      AUTHORITY_LINK, List.of("auth-link-v1"),
      EQUIVALENT, List.of("equiv-v1"),
      LINKAGE, List.of("linkage-v1"),
      CONTROL_FIELD, List.of("ctrl-v1"),
      FIELD_LINK, List.of("fl-v1")
    )));

    // when
    resourceGraphService.saveMergingGraph(createOrganizationResource(1L, Map.of(
      PLACE, List.of("place-v2"),
      AUTHORITY_LINK, List.of("auth-link-v2"),
      EQUIVALENT, List.of("equiv-v2"),
      LINKAGE, List.of("linkage-v2"),
      CONTROL_FIELD, List.of("ctrl-v2"),
      FIELD_LINK, List.of("fl-v2")
    )));

    // then
    var doc = resourceTestService.getResourceById("1", 1).getDoc();
    assertDocValues(doc, PLACE, List.of("place-v1", "place-v2"));
    assertDocValues(doc, AUTHORITY_LINK, List.of("auth-link-v1", "auth-link-v2"));
    assertDocValues(doc, EQUIVALENT, List.of("equiv-v1", "equiv-v2"));
    assertDocValues(doc, LINKAGE, List.of("linkage-v1", "linkage-v2"));
    assertDocValues(doc, CONTROL_FIELD, List.of("ctrl-v1", "ctrl-v2"));
    assertDocValues(doc, FIELD_LINK, List.of("fl-v1", "fl-v2"));
  }

  @Test
  void shouldMergeMeeting111MultiValuedSubfields() {
    // given
    resourceGraphService.saveMergingGraph(createMeetingResource(1L, Map.of(
      PLACE, List.of("place-v1"),
      AUTHORITY_LINK, List.of("auth-link-v1"),
      EQUIVALENT, List.of("equiv-v1"),
      LINKAGE, List.of("linkage-v1"),
      CONTROL_FIELD, List.of("ctrl-v1"),
      FIELD_LINK, List.of("fl-v1")
    )));

    // when
    resourceGraphService.saveMergingGraph(createMeetingResource(1L, Map.of(
      PLACE, List.of("place-v2"),
      AUTHORITY_LINK, List.of("auth-link-v2"),
      EQUIVALENT, List.of("equiv-v2"),
      LINKAGE, List.of("linkage-v2"),
      CONTROL_FIELD, List.of("ctrl-v2"),
      FIELD_LINK, List.of("fl-v2")
    )));

    // then
    var doc = resourceTestService.getResourceById("1", 1).getDoc();
    assertDocValues(doc, PLACE, List.of("place-v1", "place-v2"));
    assertDocValues(doc, AUTHORITY_LINK, List.of("auth-link-v1", "auth-link-v2"));
    assertDocValues(doc, EQUIVALENT, List.of("equiv-v1", "equiv-v2"));
    assertDocValues(doc, LINKAGE, List.of("linkage-v1", "linkage-v2"));
    assertDocValues(doc, CONTROL_FIELD, List.of("ctrl-v1", "ctrl-v2"));
    assertDocValues(doc, FIELD_LINK, List.of("fl-v1", "fl-v2"));
  }

  @Test
  void shouldReuseSharedConceptNodesFor610FieldsWithCommonSubfields() {
    long orgHash = 100L;
    long formHash = 200L;
    long topic1Hash = 301L;
    long topic2Hash = 302L;
    long concept1Hash = 401L;
    long concept2Hash = 402L;

    // given: first 610 field
    resourceGraphService.saveMergingGraph(
      createOrganizationConceptResource(concept1Hash,
        createConceptComponent(orgHash, ORGANIZATION, "Org Name"),
        List.of(
          createConceptComponent(formHash, FORM, "Form Subdiv"),
          createConceptComponent(topic1Hash, TOPIC, "Topic 1")
        )
      )
    );

    // when: second 610 field with same $a and $v, different $x
    resourceGraphService.saveMergingGraph(
      createOrganizationConceptResource(concept2Hash,
        createConceptComponent(orgHash, ORGANIZATION, "Org Name"),
        List.of(
          createConceptComponent(formHash, FORM, "Form Subdiv"),
          createConceptComponent(topic2Hash, TOPIC, "Topic 2")
        )
      )
    );

    // then: 6 unique resources — Organization and Form nodes are not duplicated
    assertThat(resourceTestService.countResources()).isEqualTo(6L);

    var savedConcept1 = resourceTestService.getResourceById(String.valueOf(concept1Hash), 2);
    var savedConcept2 = resourceTestService.getResourceById(String.valueOf(concept2Hash), 2);

    // both concepts reference the same Organization focus node
    assertThat(getFocusTargetId(savedConcept1)).isEqualTo(orgHash);
    assertThat(getFocusTargetId(savedConcept2)).isEqualTo(orgHash);

    // both concepts share the same Form sub-focus node
    assertThat(getSubFocusTargetIds(savedConcept1)).contains(formHash);
    assertThat(getSubFocusTargetIds(savedConcept2)).contains(formHash);
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
    return TEST_JSON_MAPPER.readTree(loadResourceAsString("samples/json_merge/existing.jsonl"));
  }

  private JsonNode getNewDoc() {
    return TEST_JSON_MAPPER.readTree(loadResourceAsString("samples/json_merge/incoming.jsonl"));
  }

  private JsonNode getMergedDoc() {
    return TEST_JSON_MAPPER.readTree(loadResourceAsString("samples/json_merge/merged.jsonl"));
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

  private Resource createPersonResource(Long hash, Map<PropertyDictionary, List<String>> properties) {
    return MonographTestUtil.createResource(properties, Set.of(PERSON), Map.of())
      .setIdAndRefreshEdges(hash);
  }

  private Resource createOrganizationResource(Long hash, Map<PropertyDictionary, List<String>> properties) {
    return MonographTestUtil.createResource(properties, Set.of(ORGANIZATION), Map.of())
      .setIdAndRefreshEdges(hash);
  }

  private Resource createMeetingResource(Long hash, Map<PropertyDictionary, List<String>> properties) {
    return MonographTestUtil.createResource(properties, Set.of(MEETING), Map.of())
      .setIdAndRefreshEdges(hash);
  }

  private void assertDocValues(tools.jackson.databind.JsonNode doc, PropertyDictionary property,
                               List<String> expectedValues) {
    var node = doc.get(property.getValue());
    assertThat(node).isNotNull();
    var actual = StreamSupport.stream(node.spliterator(), false)
      .map(n -> n.asText())
      .toList();
    assertThat(actual).containsExactlyInAnyOrderElementsOf(expectedValues);
  }

  private Resource createConceptComponent(long hash, ResourceTypeDictionary type, String name) {
    return MonographTestUtil.createResource(
      Map.of(PropertyDictionary.NAME, List.of(name)),
      Set.of(type),
      Map.of()
    ).setIdAndRefreshEdges(hash);
  }

  private Resource createOrganizationConceptResource(long hash, Resource focus, List<Resource> subFoci) {
    return MonographTestUtil.createResource(
      Map.of(PropertyDictionary.NAME, List.of("organization concept")),
      Set.of(CONCEPT, ORGANIZATION),
      new java.util.LinkedHashMap<>(Map.of(
        PredicateDictionary.FOCUS, List.of(focus),
        PredicateDictionary.SUB_FOCUS, subFoci
      ))
    ).setIdAndRefreshEdges(hash);
  }

  private long getFocusTargetId(Resource concept) {
    return concept.getOutgoingEdges().stream()
      .filter(e -> e.getPredicate().getUri().equals(PredicateDictionary.FOCUS.getUri()))
      .findFirst()
      .orElseThrow()
      .getId().getTargetHash();
  }

  private List<Long> getSubFocusTargetIds(Resource concept) {
    return concept.getOutgoingEdges().stream()
      .filter(e -> e.getPredicate().getUri().equals(PredicateDictionary.SUB_FOCUS.getUri()))
      .map(e -> e.getId().getTargetHash())
      .toList();
  }

  private void assertEdge(ResourceEdge edge, long sourceHash, long targetHash, Resource source) {
    assertThat(edge.getId().getSourceHash()).isEqualTo(sourceHash);
    assertThat(edge.getId().getTargetHash()).isEqualTo(targetHash);
    assertThat(edge.getSource()).isEqualTo(source);
  }
}
