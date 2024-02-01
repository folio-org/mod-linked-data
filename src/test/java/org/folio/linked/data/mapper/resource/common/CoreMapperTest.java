package org.folio.linked.data.mapper.resource.common;

import static org.folio.ld.dictionary.PropertyDictionary.LABEL_RDF;
import static org.folio.ld.dictionary.PropertyDictionary.NAME;
import static org.folio.ld.dictionary.PropertyDictionary.QUALIFIER;
import static org.folio.linked.data.test.IsEqualJson.equalToJson;
import static org.folio.linked.data.test.TestUtil.OBJECT_MAPPER;
import static org.folio.linked.data.test.TestUtil.getJsonNode;
import static org.folio.linked.data.test.TestUtil.getObjectNode;
import static org.folio.linked.data.test.TestUtil.getSampleInstanceString;
import static org.folio.linked.data.test.TestUtil.randomLong;
import static org.folio.linked.data.util.Constants.TYPE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.ld.dictionary.api.Predicate;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.domain.dto.InstanceField;
import org.folio.linked.data.domain.dto.Isbn;
import org.folio.linked.data.mapper.resource.monograph.common.StatusMapperUnit;
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
  void addMappedResources_shouldThrowNpe_ifGivenMapperIsNull(@Mock Resource source) {
    // given
    SingleResourceMapperUnit singleResourceMapperUnit = null;
    Predicate predicate = null;
    var destination = new Isbn();

    // when
    NullPointerException thrown = assertThrows(NullPointerException.class,
      () -> coreMapper.addMappedOutgoingResources(singleResourceMapperUnit, source, predicate, destination));

    // then
    assertThat(thrown.getMessage(), is("singleResourceMapperUnit is marked non-null but is null"));
  }

  @Test
  void addMappedResources_shouldThrowNpe_ifGivenSourceIsNull(@Mock StatusMapperUnit singleResourceMapperUnit) {
    // given
    Resource source = null;
    var predicate = PredicateDictionary.MAP;
    var destination = new Isbn();

    // when
    NullPointerException thrown = assertThrows(NullPointerException.class,
      () -> coreMapper.addMappedOutgoingResources(singleResourceMapperUnit, source, predicate, destination));

    // then
    assertThat(thrown.getMessage(), is("source is marked non-null but is null"));
  }

  @Test
  void addMappedResources_shouldThrowNpe_ifGivenPredicateIsNull(@Mock StatusMapperUnit singleResourceMapperUnit,
                                                                @Mock Resource source) {
    // given
    Predicate predicate = null;
    var destination = new Isbn();

    // when
    NullPointerException thrown = assertThrows(NullPointerException.class,
      () -> coreMapper.addMappedOutgoingResources(singleResourceMapperUnit, source, predicate, destination));

    // then
    assertThat(thrown.getMessage(), is("predicate is marked non-null but is null"));
  }

  @Test
  void addMappedResources_shouldThrowNpe_ifGivenDestinationIsNull(@Mock StatusMapperUnit singleResourceMapperUnit,
                                                                  @Mock Resource source) {
    // given
    var predicate = PredicateDictionary.MAP;
    Object destination = null;

    // when
    NullPointerException thrown = assertThrows(NullPointerException.class,
      () -> coreMapper.addMappedOutgoingResources(singleResourceMapperUnit, source, predicate, destination));

    // then
    assertThat(thrown.getMessage(), is("destination is marked non-null but is null"));
  }

  @Test
  void addMappedResources_shouldAddMappedResources_ifGivenDataIsCorrect(
    @Mock StatusMapperUnit singleResourceMapperUnit) {
    // given
    var predicate = PredicateDictionary.MAP;
    var targetResource = new Resource().setLabel("target");
    var notTargetResource = new Resource().setLabel("notTarget");
    var source = new Resource().setLabel("source");
    source.getOutgoingEdges()
      .add(new ResourceEdge(source, targetResource, new PredicateEntity(predicate.getUri())));
    source.getOutgoingEdges()
      .add(new ResourceEdge(source, notTargetResource, new PredicateEntity("not " + predicate.getUri())));
    var destination = new Isbn();

    // when
    coreMapper.addMappedOutgoingResources(singleResourceMapperUnit, source, predicate, destination);

    // then
    verify(singleResourceMapperUnit).toDto(targetResource, destination, null);
  }

  @Test
  void addDtoWithEdges_shouldThrowNpe_ifGivenResourceIsNull(@Mock Consumer<Instance> consumer) {
    // given
    Resource resource = null;

    // when
    NullPointerException thrown = assertThrows(NullPointerException.class,
      () -> coreMapper.mapToDtoWithEdges(resource, consumer, Instance.class));

    // then
    assertThat(thrown.getMessage(), is("resource is marked non-null but is null"));
  }

  @Test
  void addDtoWithEdges_shouldThrowNpe_ifGivenConsumerIsNull(@Mock Resource resource) {
    // given
    Consumer<Instance> consumer = null;

    // when
    NullPointerException thrown = assertThrows(NullPointerException.class,
      () -> coreMapper.mapToDtoWithEdges(resource, consumer, Instance.class));

    // then
    assertThat(thrown.getMessage(), is("consumer is marked non-null but is null"));
  }

  @Test
  void addDtoWithEdges_shouldThrowNpe_ifGivenDestinationIsNull(@Mock Consumer<Instance> consumer,
                                                               @Mock Resource resource) {
    // given
    Class destination = null;

    // when
    NullPointerException thrown = assertThrows(NullPointerException.class,
      () -> coreMapper.mapToDtoWithEdges(resource, consumer, destination));

    // then
    assertThat(thrown.getMessage(), is("destination is marked non-null but is null"));
  }

  @Test
  void addDtoWithEdges_shouldAddDestinationObjectToGivenConsumer_ifGivenResourceIsEmpty() {
    // given
    var instanceField = new InstanceField();
    var resource = new Resource();

    // when
    coreMapper.mapToDtoWithEdges(resource, instanceField::setInstance, Instance.class);

    // then
    assertThat(instanceField.getInstance(), notNullValue());
  }

  @Test
  void addDtoWithEdges_shouldAddDestinationObjectToGivenConsumer_ifGivenResourceContainsDoc() {
    // given
    var instanceField = new InstanceField();
    var node = OBJECT_MAPPER.createObjectNode();
    var resource = new Resource().setDoc(node);

    // when
    coreMapper.mapToDtoWithEdges(resource, instanceField::setInstance, Instance.class);

    // then
    assertThat(instanceField.getInstance(), notNullValue());
  }

  @Test
  void addDtoWithEdges_shouldAddDestinationObjectToGivenConsumerAndMapEdge_ifGivenResourceContainsDocAndEdge() {
    // given
    var instanceField = new InstanceField();
    var node = OBJECT_MAPPER.createObjectNode();
    var resource = new Resource().setDoc(node);
    var target = new Resource().setLabel("target").addType(ResourceTypeDictionary.INSTANCE);
    var edge = new ResourceEdge(resource, target, new PredicateEntity("pred"));
    resource.setOutgoingEdges(Set.of(edge));

    // when
    coreMapper.mapToDtoWithEdges(resource, instanceField::setInstance, Instance.class);

    // then
    assertThat(instanceField.getInstance(), notNullValue());
    verify(singleResourceMapper).toDto(eq(edge.getSource()), any(Instance.class), eq(resource),
      eq(edge.getPredicate()));
  }

  @Test
  void readResourceDoc_shouldThrowNpe_ifGivenResourceIsNull() {
    // given
    Resource resource = null;
    var dtoClass = Instance.class;

    // when
    NullPointerException thrown = assertThrows(NullPointerException.class,
      () -> coreMapper.readResourceDoc(resource, dtoClass));

    // then
    assertThat(thrown.getMessage(), is("resource is marked non-null but is null"));
  }

  @Test
  void readResourceDoc_shouldThrowNpe_ifGivenDtoClassIsNull() {
    // given
    var resource = new Resource();
    Class dtoClass = null;

    // when
    NullPointerException thrown = assertThrows(NullPointerException.class,
      () -> coreMapper.readResourceDoc(resource, dtoClass));

    // then
    assertThat(thrown.getMessage(), is("dtoClass is marked non-null but is null"));
  }

  @Test
  void readResourceDoc_shouldReturnEmptyDto_ifGivenResourceHasNoDoc() {
    // given
    var resource = new Resource();
    var dtoClass = Isbn.class;

    // when
    var result = coreMapper.readResourceDoc(resource, dtoClass);

    // then
    assertThat(result, is(new Isbn()));
  }

  @Test
  void readResourceDoc_shouldReturnCorrectDto_ifGivenResourceHasDoc() {
    // given
    var names = List.of(UUID.randomUUID().toString());
    var qualifiers = List.of(UUID.randomUUID().toString());
    var node = getJsonNode(Map.of(NAME.getValue(), names, QUALIFIER.getValue(), qualifiers));
    var resource = new Resource().setDoc(node);
    var dtoClass = Isbn.class;

    // when
    var result = coreMapper.readResourceDoc(resource, dtoClass);

    // then
    assertThat(result.getValue(), is(names));
    assertThat(result.getQualifier(), is(qualifiers));
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
    var json = getSampleInstanceString();

    // when
    var jsonNode = coreMapper.toJson(json);

    // then
    assertThat(OBJECT_MAPPER.writeValueAsString(jsonNode), equalToJson(getSampleInstanceString()));
  }

  @Test
  void toJson_shouldReturnCorrectJsonNodeFromMap() throws JsonProcessingException {
    // given
    var json = getSampleInstanceString();
    var map = OBJECT_MAPPER.readValue(json, new TypeReference<Map<String, Object>>() {
    });

    // when
    var jsonNode = coreMapper.toJson(map);

    // then
    assertThat(OBJECT_MAPPER.writeValueAsString(jsonNode), equalToJson(getSampleInstanceString()));
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
  void mapSubEdges_shouldThrowNpe_ifGivenSourceIsNull(@Mock StatusMapperUnit mapper) {
    // given
    var dtoList = new ArrayList<>();
    Resource source = null;
    var predicate = PredicateDictionary.STATUS;

    // when
    NullPointerException thrown = assertThrows(NullPointerException.class,
      () -> coreMapper.mapSubEdges(dtoList, source, predicate, dto -> mapper.toEntity(dto, null)));

    // then
    assertThat(thrown.getMessage(), is("source is marked non-null but is null"));
  }

  @Test
  void mapSubEdges_shouldThrowNpe_ifGivenPredicateIsNull(@Mock StatusMapperUnit mapper) {
    // given
    var dtoList = new ArrayList<>();
    var source = new Resource();
    PredicateDictionary predicate = null;

    // when
    NullPointerException thrown = assertThrows(NullPointerException.class,
      () -> coreMapper.mapSubEdges(dtoList, source, predicate, dto -> mapper.toEntity(dto, null)));

    // then
    assertThat(thrown.getMessage(), is("predicate is marked non-null but is null"));
  }

  @Test
  void mapSubEdges_shouldThrowNpe_ifGivenMappingFunctionIsNull() {
    // given
    var dtoList = new ArrayList<>();
    var source = new Resource();
    var predicate = PredicateDictionary.MAP;

    // when
    NullPointerException thrown = assertThrows(NullPointerException.class,
      () -> coreMapper.mapSubEdges(dtoList, source, predicate, null));

    // then
    assertThat(thrown.getMessage(), is("mappingFunction is marked non-null but is null"));
  }

  @Test
  void mapSubEdges_shouldDoNothing_ifGivenDtoListIsNull(@Mock StatusMapperUnit mapper) {
    // given
    List dtoList = null;
    var source = new Resource();
    var predicate = PredicateDictionary.MAP;

    // when
    coreMapper.mapSubEdges(dtoList, source, predicate, dto -> mapper.toEntity(dto, null));

    // then
    verify(mapper, never()).toEntity(any(), any());
    assertThat(source.getOutgoingEdges(), hasSize(0));
  }

  @Test
  void mapSubEdges_shouldDoNothing_ifGivenDtoListIsEmpty(@Mock StatusMapperUnit mapper) {
    // given
    var dtoList = new ArrayList<>();
    var source = new Resource();
    var predicate = PredicateDictionary.MAP;

    // when
    coreMapper.mapSubEdges(dtoList, source, predicate, dto -> mapper.toEntity(dto, null));

    // then
    verify(mapper, never()).toEntity(any(), any());
    assertThat(source.getOutgoingEdges(), hasSize(0));
  }

  @Test
  void mapSubEdges_shouldAddMappedEdgesToResource_ifGivenDtoListIsNotEmptyAndNoType(
    @Mock StatusMapperUnit mapper) {
    // given
    var dto1 = new Isbn().id(randomLong().toString());
    var dto2 = new Isbn().id(randomLong().toString());
    var predicate = PredicateDictionary.MAP;
    var expectedTarget1 = new Resource().setDoc(new TextNode("1")).setResourceHash(111L);
    doReturn(expectedTarget1).when(mapper).toEntity(dto1, null);
    var expectedTarget2 = new Resource().setDoc(new TextNode("2")).setResourceHash(222L);
    doReturn(expectedTarget2).when(mapper).toEntity(dto2, null);
    var source = new Resource();
    var dtoList = List.of(dto1, dto2);

    // when
    coreMapper.mapSubEdges(dtoList, source, predicate, dto -> mapper.toEntity(dto, null));

    // then
    assertThat(source.getOutgoingEdges(), hasSize(2));
    var edgesAreExpected = source.getOutgoingEdges().stream().allMatch(edge ->
      edge.getPredicate().getUri().equals(predicate.getUri())
        && edge.getSource().equals(source)
        && (edge.getTarget().equals(expectedTarget1)) || edge.getTarget().equals(expectedTarget2));
    assertThat(edgesAreExpected, is(true));
  }

  @Test
  void mapSubEdges_shouldAddMappedEdgesToResource_ifGivenDtoListIsNotEmptyAndType(
    @Mock StatusMapperUnit mapper) {
    // given
    var dto1 = new Isbn().id(randomLong().toString());
    var dto2 = new Isbn().id(randomLong().toString());
    var predicate = PredicateDictionary.MAP;
    var expectedTarget1 = new Resource().setDoc(new TextNode("1")).setResourceHash(111L);
    doReturn(expectedTarget1).when(mapper).toEntity(dto1, null);
    var expectedTarget2 = new Resource().setDoc(new TextNode("2")).setResourceHash(222L);
    doReturn(expectedTarget2).when(mapper).toEntity(dto2, null);
    var source = new Resource();
    var dtoList = List.of(dto1, dto2);

    // when
    coreMapper.mapSubEdges(dtoList, source, predicate, dto -> mapper.toEntity(dto, null));

    // then
    assertThat(source.getOutgoingEdges(), hasSize(2));
    var edgesAreExpected = source.getOutgoingEdges().stream().allMatch(edge ->
      edge.getPredicate().getUri().equals(predicate.getUri())
        && edge.getSource().equals(source)
        && (edge.getTarget().equals(expectedTarget1)) || edge.getTarget().equals(expectedTarget2));
    assertThat(edgesAreExpected, is(true));
  }

  @Test
  void toOutgoingEdges_shouldThrowNpe_ifGivenSourceIsNull() {
    // given
    var dtoList = new ArrayList<>();
    Resource source = null;
    var predicate = PredicateDictionary.MAP;
    var parent = Instance.class;

    // when
    NullPointerException thrown = assertThrows(NullPointerException.class,
      () -> coreMapper.toOutgoingEdges(dtoList, source, predicate, parent));

    // then
    assertThat(thrown.getMessage(), is("parentEntity is marked non-null but is null"));
  }

  @Test
  void toOutgoingEdges_shouldThrowNpe_ifGivenPredicateIsNull() {
    // given
    var dtoList = new ArrayList<>();
    var source = new Resource();
    Predicate predicate = null;
    var parent = Instance.class;

    // when
    NullPointerException thrown = assertThrows(NullPointerException.class,
      () -> coreMapper.toOutgoingEdges(dtoList, source, predicate, parent));

    // then
    assertThat(thrown.getMessage(), is("predicate is marked non-null but is null"));
  }

  @Test
  void toOutgoingEdges_shouldThrowNpe_ifGivenParentIsNull() {
    // given
    var dtoList = new ArrayList<>();
    var source = new Resource();
    var predicate = PredicateDictionary.MAP;
    Class<Object> parent = null;

    // when
    NullPointerException thrown = assertThrows(NullPointerException.class,
      () -> coreMapper.toOutgoingEdges(dtoList, source, predicate, parent));

    // then
    assertThat(thrown.getMessage(), is("parentDtoClass is marked non-null but is null"));
  }

  @Test
  void toOutgoingEdges_shouldDoNothing_ifGivenDtoListIsNull() {
    // given
    List dtoList = null;
    var source = new Resource();
    var predicate = PredicateDictionary.MAP;
    var parent = Instance.class;

    // when
    var result = coreMapper.toOutgoingEdges(dtoList, source, predicate, parent);

    // then
    verify(singleResourceMapper, never()).toEntity(any(), any(), any(), any());
    assertThat(result.isEmpty(), is(true));
  }

  @Test
  void toOutgoingEdges_shouldDoNothing_ifGivenDtoListIsEmpty() {
    // given
    var dtoList = new ArrayList<>();
    var source = new Resource();
    var predicate = PredicateDictionary.MAP;
    var parent = Instance.class;

    // when
    var result = coreMapper.toOutgoingEdges(dtoList, source, predicate, parent);

    // then
    verify(singleResourceMapper, never()).toEntity(any(), any(), any(), any());
    assertThat(result, hasSize(0));
  }

  @Test
  void toOutgoingEdges_shouldAddMappedEdgesToResource_ifGivenDtoListIsNotEmpty() {
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
    var result = coreMapper.toOutgoingEdges(dtoList, source, predicate, parent);

    // then
    assertThat(result, hasSize(2));
    var edgesAreExpected = source.getOutgoingEdges().stream().allMatch(edge ->
      edge.getPredicate().getHash().equals(predicate.getHash())
        && edge.getSource().equals(source)
        && (edge.getTarget().equals(expectedTarget1)) || edge.getTarget().equals(expectedTarget2));
    assertThat(edgesAreExpected, is(true));
  }

}
