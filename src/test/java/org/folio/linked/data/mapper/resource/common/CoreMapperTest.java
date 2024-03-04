package org.folio.linked.data.mapper.resource.common;

import static org.folio.ld.dictionary.PropertyDictionary.LABEL_RDF;
import static org.folio.ld.dictionary.PropertyDictionary.NAME;
import static org.folio.ld.dictionary.PropertyDictionary.QUALIFIER;
import static org.folio.linked.data.test.IsEqualJson.equalToJson;
import static org.folio.linked.data.test.TestUtil.BIBFRAME_SAMPLE;
import static org.folio.linked.data.test.TestUtil.OBJECT_MAPPER;
import static org.folio.linked.data.test.TestUtil.getJsonNode;
import static org.folio.linked.data.test.TestUtil.getObjectNode;
import static org.folio.linked.data.test.TestUtil.randomLong;
import static org.folio.linked.data.util.Constants.TYPE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.ld.dictionary.model.Predicate;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.domain.dto.Isbn;
import org.folio.linked.data.model.entity.PredicateEntity;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.model.entity.ResourceTypeEntity;
import org.folio.linked.data.util.HashUtil;
import org.folio.spring.test.type.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class CoreMapperTest {

  @InjectMocks
  private CoreMapperImpl coreMapper;
  @Spy
  private ObjectMapper objectMapper = OBJECT_MAPPER;
  @Mock
  private SingleResourceMapper singleResourceMapper;

  @BeforeEach
  void setUp() {
    reset(singleResourceMapper);
  }

  @Test
  void toDtoWithEdges_shouldThrowNpe_ifGivenResourceIsNull(@Mock Consumer<Instance> consumer) {
    // given
    Resource resource = null;

    // when
    NullPointerException thrown = assertThrows(NullPointerException.class,
      () -> coreMapper.toDtoWithEdges(resource, Instance.class, false));

    // then
    assertThat(thrown.getMessage(), is("resource is marked non-null but is null"));
  }

  @Test
  void toDtoWithEdges_shouldThrowNpe_ifGivenDtoClassIsNull(@Mock Resource resource) {
    // given
    Class dtoClass = null;

    // when
    NullPointerException thrown = assertThrows(NullPointerException.class,
      () -> coreMapper.toDtoWithEdges(resource, dtoClass, false));

    // then
    assertThat(thrown.getMessage(), is("dtoClass is marked non-null but is null"));
  }

  @Test
  void hash_shouldThrowNpe_ifGivenResourceIsNull() {
    // given
    Resource resource = null;

    // when
    NullPointerException thrown = assertThrows(NullPointerException.class, () -> coreMapper.hash(resource));

    // then
    assertThat(thrown.getMessage(), is("resource is marked non-null but is null"));
  }

  @Test
  void hash_shouldReturnHashUtilResultForResourceDocAndLabelAndType_ifGivenResourceContainsDoc() {
    // given
    var names = List.of(UUID.randomUUID().toString());
    var qualifiers = List.of(UUID.randomUUID().toString());
    var node = getJsonNode(Map.of(NAME.getValue(), names, QUALIFIER.getValue(), qualifiers));
    var resource = new Resource().setDoc(node).addType(new ResourceTypeEntity());
    ObjectNode expectedNodeForHash = resource.getDoc().deepCopy();
    expectedNodeForHash.put(LABEL_RDF.getValue(), resource.getLabel());
    expectedNodeForHash.put(TYPE, resource.getTypes().iterator().next().getHash());

    // when
    var result = coreMapper.hash(resource);

    // then
    assertThat(result, is(HashUtil.hash(expectedNodeForHash)));
  }

  @Test
  void hash_shouldReturnHashUtilResultForLabelAndType_ifGivenResourceContainsNoDoc() {
    // given
    var resource = new Resource().addType(new ResourceTypeEntity());
    var expectedNodeForHash = OBJECT_MAPPER.createObjectNode();
    expectedNodeForHash.put(LABEL_RDF.getValue(), resource.getLabel());
    expectedNodeForHash.put(TYPE, resource.getTypes().iterator().next().getHash());

    // when
    var result = coreMapper.hash(resource);

    // then
    assertThat(result, is(HashUtil.hash(expectedNodeForHash)));
  }

  @Test
  void hash_shouldReturnHashUtilResultForNodeOfEdgeJsons_ifGivenResourceContainsNoDoc() {
    // given
    var resource = new Resource().addType(new ResourceTypeEntity());
    var targetNode1 = getObjectNode("label", "name", "link");
    var target1 = new Resource().setDoc(targetNode1).setResourceHash(111L).addType(new ResourceTypeEntity());
    var predicate1 = new PredicateEntity("predicate1");
    var targetNode2 = getObjectNode("label2", "name2", "link2");
    var target2 = new Resource().setDoc(targetNode2).setResourceHash(222L).addType(new ResourceTypeEntity());
    var predicate2 = new PredicateEntity("predicate2");
    var targetNode3 = getObjectNode("label3", "name3", "link3");
    var target3 = new Resource().setDoc(targetNode3).setResourceHash(333L).addType(new ResourceTypeEntity());
    resource.getOutgoingEdges()
      .add(new ResourceEdge(resource, target1, predicate1));
    resource.getOutgoingEdges()
      .add(new ResourceEdge(resource, target2, predicate1));
    resource.getOutgoingEdges()
      .add(new ResourceEdge(resource, target3, predicate2));

    var expectedNodeForHash = OBJECT_MAPPER.createObjectNode();
    expectedNodeForHash.put(LABEL_RDF.getValue(), resource.getLabel());
    expectedNodeForHash.put(TYPE, resource.getTypes().iterator().next().getHash());
    var arrayPredicate1 = OBJECT_MAPPER.createArrayNode();
    arrayPredicate1.add(targetNode1.deepCopy()
      .put(LABEL_RDF.getValue(), target1.getLabel())
      .put(TYPE, target1.getTypes().iterator().next().getHash())
    );
    arrayPredicate1.add(targetNode2.deepCopy()
      .put(LABEL_RDF.getValue(), target2.getLabel())
      .put(TYPE, target2.getTypes().iterator().next().getHash())
    );
    expectedNodeForHash.set(predicate1.getUri(), arrayPredicate1);
    var arrayPredicate2 = OBJECT_MAPPER.createArrayNode();
    arrayPredicate2.add(targetNode3.deepCopy()
      .put(LABEL_RDF.getValue(), target3.getLabel())
      .put(TYPE, target3.getTypes().iterator().next().getHash())
    );
    expectedNodeForHash.set(predicate2.getUri(), arrayPredicate2);

    // when
    var result = coreMapper.hash(resource);

    // then
    assertThat(result, is(HashUtil.hash(expectedNodeForHash)));
  }

  @Test
  void hash_shouldReturnHashUtilResultForNodeOfDocAndEdgeJsons_ifGivenResourceContainsDocAndEdges() {
    // given
    var rootNode = getObjectNode("label", "name", "link");
    var resource = new Resource().setDoc(rootNode).addType(new ResourceTypeEntity());
    var targetNode1 = getObjectNode("label2", "name2", "link2");
    var target1 = new Resource().setDoc(targetNode1).setResourceHash(111L).addType(new ResourceTypeEntity());
    var predicate1 = new PredicateEntity("predicate1");
    var targetNode2 = getObjectNode("label3", "name3", "link3");
    var target2 = new Resource().setDoc(targetNode2).setResourceHash(222L).addType(new ResourceTypeEntity());
    var predicate2 = new PredicateEntity("predicate2");
    var targetNode3 = getObjectNode("label4", "name4", "link4");
    var target3 = new Resource().setDoc(targetNode3).setResourceHash(333L).addType(new ResourceTypeEntity());
    resource.getOutgoingEdges()
      .add(new ResourceEdge(resource, target1, predicate1));
    resource.getOutgoingEdges()
      .add(new ResourceEdge(resource, target2, predicate1));
    resource.getOutgoingEdges()
      .add(new ResourceEdge(resource, target3, predicate2));

    var expectedNodeForHash = rootNode.deepCopy();
    expectedNodeForHash.put(LABEL_RDF.getValue(), resource.getLabel());
    expectedNodeForHash.put(TYPE, resource.getTypes().iterator().next().getHash());
    var arrayPredicate1 = OBJECT_MAPPER.createArrayNode();
    arrayPredicate1.add(targetNode1.deepCopy()
      .put(LABEL_RDF.getValue(), target1.getLabel())
      .put(TYPE, target1.getTypes().iterator().next().getHash())
    );
    arrayPredicate1.add(targetNode2.deepCopy()
      .put(LABEL_RDF.getValue(), target2.getLabel())
      .put(TYPE, target2.getTypes().iterator().next().getHash())
    );
    expectedNodeForHash.set(predicate1.getUri(), arrayPredicate1);
    var arrayPredicate2 = OBJECT_MAPPER.createArrayNode();
    arrayPredicate2.add(targetNode3.deepCopy()
      .put(LABEL_RDF.getValue(), target3.getLabel())
      .put(TYPE, target3.getTypes().iterator().next().getHash())
    );
    expectedNodeForHash.set(predicate2.getUri(), arrayPredicate2);

    // when
    var result = coreMapper.hash(resource);

    // then
    assertThat(result, is(HashUtil.hash(expectedNodeForHash)));
  }

  @Test
  void toJson_shouldReturnCorrectJsonNodeFromString() throws JsonProcessingException {
    // given
    var json = BIBFRAME_SAMPLE;

    // when
    var jsonNode = coreMapper.toJson(json);

    // then
    assertThat(OBJECT_MAPPER.writeValueAsString(jsonNode), equalToJson(BIBFRAME_SAMPLE));
  }

  @Test
  void toJson_shouldReturnCorrectJsonNodeFromMap() throws JsonProcessingException {
    // given
    var json = BIBFRAME_SAMPLE;
    var map = OBJECT_MAPPER.readValue(json, new TypeReference<Map<String, Object>>() {
    });

    // when
    var jsonNode = coreMapper.toJson(map);

    // then
    assertThat(OBJECT_MAPPER.writeValueAsString(jsonNode), equalToJson(BIBFRAME_SAMPLE));
  }

  @Test
  void toJson_shouldReturnEmptyJsonNodeForNullInput() throws JsonProcessingException {
    // given
    Object object = null;

    // when
    var jsonNode = coreMapper.toJson(object);

    // then
    assertThat(OBJECT_MAPPER.writeValueAsString(jsonNode), equalToJson("{}"));
  }

  @Test
  void addOutgoingEdges_shouldThrowNpe_ifGivenSourceIsNull() {
    // given
    var dtoList = new ArrayList<>();
    Resource source = null;
    var predicate = PredicateDictionary.MAP;
    var parent = Instance.class;

    // when
    NullPointerException thrown = assertThrows(NullPointerException.class,
      () -> coreMapper.addOutgoingEdges(source, parent, dtoList, predicate));

    // then
    assertThat(thrown.getMessage(), is("parentEntity is marked non-null but is null"));
  }

  @Test
  void addOutgoingEdges_shouldThrowNpe_ifGivenPredicateIsNull() {
    // given
    var dtoList = new ArrayList<>();
    var source = new Resource();
    Predicate predicate = null;
    var parent = Instance.class;

    // when
    NullPointerException thrown = assertThrows(NullPointerException.class,
      () -> coreMapper.addOutgoingEdges(source, parent, dtoList, predicate));

    // then
    assertThat(thrown.getMessage(), is("predicate is marked non-null but is null"));
  }

  @Test
  void addOutgoingEdges_shouldThrowNpe_ifGivenParentIsNull() {
    // given
    var dtoList = new ArrayList<>();
    var source = new Resource();
    var predicate = PredicateDictionary.MAP;
    Class<Object> parent = null;

    // when
    NullPointerException thrown = assertThrows(NullPointerException.class,
      () -> coreMapper.addOutgoingEdges(source, parent, dtoList, predicate));

    // then
    assertThat(thrown.getMessage(), is("parentDtoClass is marked non-null but is null"));
  }

  @Test
  void addOutgoingEdges_shouldDoNothing_ifGivenDtoListIsNull() {
    // given
    List dtoList = null;
    var source = new Resource();
    var predicate = PredicateDictionary.MAP;
    var parent = Instance.class;

    // when
    coreMapper.addOutgoingEdges(source, parent, dtoList, predicate);

    // then
    verify(singleResourceMapper, never()).toEntity(any(), any(), any(), any());
    assertThat(source.getOutgoingEdges().isEmpty(), is(true));
  }

  @Test
  void addOutgoingEdges_shouldDoNothing_ifGivenDtoListIsEmpty() {
    // given
    var dtoList = new ArrayList<>();
    var source = new Resource();
    var predicate = PredicateDictionary.MAP;
    var parent = Instance.class;

    // when
    coreMapper.addOutgoingEdges(source, parent, dtoList, predicate);

    // then
    verify(singleResourceMapper, never()).toEntity(any(), any(), any(), any());
    assertThat(source.getOutgoingEdges(), hasSize(0));
  }

  @Test
  void addOutgoingEdges_shouldAddMappedEdgesToResource_ifGivenDtoListIsNotEmpty() {
    // given
    var dto1 = new Isbn().id(randomLong().toString());
    var dto2 = new Isbn().id(randomLong().toString());
    var predicate = PredicateDictionary.MAP;
    var parent = Instance.class;
    var source = new Resource();
    var expectedTarget1 = new Resource().setDoc(new TextNode("1")).setResourceHash(111L);
    doReturn(expectedTarget1).when(singleResourceMapper).toEntity(dto1, parent, predicate, source);
    var expectedTarget2 = new Resource().setDoc(new TextNode("2")).setResourceHash(222L);
    doReturn(expectedTarget2).when(singleResourceMapper).toEntity(dto2, parent, predicate, source);
    var dtoList = List.of(dto1, dto2);

    // when
    coreMapper.addOutgoingEdges(source, parent, dtoList, predicate);

    // then
    assertThat(source.getOutgoingEdges(), hasSize(2));
    var edgesAreExpected = source.getOutgoingEdges().stream().allMatch(edge ->
      edge.getPredicate().getHash().equals(predicate.getHash())
        && edge.getSource().equals(source)
        && (edge.getTarget().equals(expectedTarget1)) || edge.getTarget().equals(expectedTarget2));
    assertThat(edgesAreExpected, is(true));
  }

}
