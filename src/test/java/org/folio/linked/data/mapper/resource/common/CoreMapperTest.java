package org.folio.linked.data.mapper.resource.common;

import static org.folio.linked.data.test.IsEqualJson.equalToJson;
import static org.folio.linked.data.test.TestUtil.OBJECT_MAPPER;
import static org.folio.linked.data.test.TestUtil.getBibframeSample;
import static org.folio.linked.data.test.TestUtil.getJsonNode;
import static org.folio.linked.data.test.TestUtil.getObjectNode;
import static org.folio.linked.data.test.TestUtil.randomLong;
import static org.folio.linked.data.util.BibframeConstants.LABEL_RDF;
import static org.folio.linked.data.util.BibframeConstants.NAME;
import static org.folio.linked.data.util.BibframeConstants.QUALIFIER;
import static org.folio.linked.data.util.BibframeConstants.TYPE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.domain.dto.InstanceField;
import org.folio.linked.data.domain.dto.Isbn;
import org.folio.linked.data.mapper.resource.common.inner.InnerResourceMapper;
import org.folio.linked.data.mapper.resource.common.inner.sub.SubResourceMapper;
import org.folio.linked.data.mapper.resource.common.inner.sub.SubResourceMapperUnit;
import org.folio.linked.data.mapper.resource.monograph.inner.common.StatusMapperUnit;
import org.folio.linked.data.model.entity.Predicate;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.model.entity.ResourceType;
import org.folio.linked.data.service.dictionary.DictionaryService;
import org.folio.linked.data.util.HashUtil;
import org.folio.spring.test.type.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class CoreMapperTest {

  private CoreMapper coreMapper;
  @Mock
  private DictionaryService<ResourceType> resourceTypeService;
  @Mock
  private DictionaryService<Predicate> predicateService;

  @BeforeEach
  void setUp() {
    coreMapper = new CoreMapperImpl(resourceTypeService, predicateService, OBJECT_MAPPER);
  }

  @Test
  void addMappedResources_shouldThrowNpe_ifGivenMapperIsNull(@Mock Resource source) {
    // given
    SubResourceMapperUnit subResourceMapperUnit = null;
    String predicate = null;
    var destination = new Isbn();

    // when
    NullPointerException thrown = assertThrows(NullPointerException.class,
      () -> coreMapper.addMappedResources(subResourceMapperUnit, source, predicate, destination));

    // then
    assertThat(thrown.getMessage(), is("subResourceMapperUnit is marked non-null but is null"));
  }

  @Test
  void addMappedResources_shouldThrowNpe_ifGivenSourceIsNull(@Mock StatusMapperUnit subResourceMapperUnit) {
    // given
    Resource source = null;
    var predicate = "predicate";
    var destination = new Isbn();

    // when
    NullPointerException thrown = assertThrows(NullPointerException.class,
      () -> coreMapper.addMappedResources(subResourceMapperUnit, source, predicate, destination));

    // then
    assertThat(thrown.getMessage(), is("source is marked non-null but is null"));
  }

  @Test
  void addMappedResources_shouldThrowNpe_ifGivenPredicateIsNull(@Mock StatusMapperUnit subResourceMapperUnit,
                                                                @Mock Resource source) {
    // given
    String predicate = null;
    var destination = new Isbn();

    // when
    NullPointerException thrown = assertThrows(NullPointerException.class,
      () -> coreMapper.addMappedResources(subResourceMapperUnit, source, predicate, destination));

    // then
    assertThat(thrown.getMessage(), is("predicate is marked non-null but is null"));
  }

  @Test
  void addMappedResources_shouldThrowNpe_ifGivenDestinationIsNull(@Mock StatusMapperUnit subResourceMapperUnit,
                                                                  @Mock Resource source) {
    // given
    var predicate = "predicate";
    Object destination = null;

    // when
    NullPointerException thrown = assertThrows(NullPointerException.class,
      () -> coreMapper.addMappedResources(subResourceMapperUnit, source, predicate, destination));

    // then
    assertThat(thrown.getMessage(), is("destination is marked non-null but is null"));
  }

  @Test
  void addMappedResources_shouldAddMappedResources_ifGivenDataIsCorrect(
    @Mock StatusMapperUnit<Isbn> subResourceMapperUnit) {
    // given
    var predicate = "predicate";
    var targetResource = new Resource().setLabel("target");
    var notTargetResource = new Resource().setLabel("notTarget");
    var source = new Resource().setLabel("source");
    source.getOutgoingEdges()
      .add(new ResourceEdge(source, targetResource, new Predicate(predicate)));
    source.getOutgoingEdges()
      .add(new ResourceEdge(source, notTargetResource, new Predicate("not " + predicate)));
    var destination = new Isbn();

    // when
    coreMapper.addMappedResources(subResourceMapperUnit, source, predicate, destination);

    // then
    verify(subResourceMapperUnit).toDto(targetResource, destination);
  }

  @Test
  void mapWithResources_shouldThrowNpe_ifGivenSubResourceMapperIsNull(@Mock Resource resource,
                                                                      @Mock Consumer<Instance> consumer) {
    // given
    SubResourceMapper subResourceMapper = null;

    // when
    NullPointerException thrown = assertThrows(NullPointerException.class,
      () -> coreMapper.mapWithResources(subResourceMapper, resource, consumer, Instance.class));

    // then
    assertThat(thrown.getMessage(), is("subResourceMapper is marked non-null but is null"));
  }

  @Test
  void mapWithResources_shouldThrowNpe_ifGivenResourceIsNull(@Mock SubResourceMapper subResourceMapper,
                                                             @Mock Consumer<Instance> consumer) {
    // given
    Resource resource = null;

    // when
    NullPointerException thrown = assertThrows(NullPointerException.class,
      () -> coreMapper.mapWithResources(subResourceMapper, resource, consumer, Instance.class));

    // then
    assertThat(thrown.getMessage(), is("resource is marked non-null but is null"));
  }

  @Test
  void mapWithResources_shouldThrowNpe_ifGivenConsumerIsNull(@Mock SubResourceMapper subResourceMapper,
                                                             @Mock Resource resource) {
    // given
    Consumer<Instance> consumer = null;

    // when
    NullPointerException thrown = assertThrows(NullPointerException.class,
      () -> coreMapper.mapWithResources(subResourceMapper, resource, consumer, Instance.class));

    // then
    assertThat(thrown.getMessage(), is("consumer is marked non-null but is null"));
  }

  @Test
  void mapWithResources_shouldThrowNpe_ifGivenDestinationIsNull(@Mock SubResourceMapper subResourceMapper,
                                                                @Mock Consumer<Instance> consumer,
                                                                @Mock Resource resource) {
    // given
    Class destination = null;

    // when
    NullPointerException thrown = assertThrows(NullPointerException.class,
      () -> coreMapper.mapWithResources(subResourceMapper, resource, consumer, destination));

    // then
    assertThat(thrown.getMessage(), is("destination is marked non-null but is null"));
  }

  @Test
  void mapWithResources_shouldAddDestinationObjectToGivenConsumer_ifGivenResourceIsEmpty(
    @Mock SubResourceMapper subResourceMapper) {
    // given
    var instanceField = new InstanceField();
    var resource = new Resource();

    // when
    coreMapper.mapWithResources(subResourceMapper, resource, instanceField::setInstance, Instance.class);

    // then
    assertThat(instanceField.getInstance(), notNullValue());
  }

  @Test
  void mapWithResources_shouldAddDestinationObjectToGivenConsumer_ifGivenResourceContainsDoc(
    @Mock SubResourceMapper subResourceMapper) {
    // given
    var instanceField = new InstanceField();
    var node = OBJECT_MAPPER.createObjectNode();
    var resource = new Resource().setDoc(node);

    // when
    coreMapper.mapWithResources(subResourceMapper, resource, instanceField::setInstance, Instance.class);

    // then
    assertThat(instanceField.getInstance(), notNullValue());
  }

  @Test
  void mapWithResources_shouldAddDestinationObjectToGivenConsumerAndMapEdge_ifGivenResourceContainsDocAndEdge(
    @Mock SubResourceMapper subResourceMapper) {
    // given
    var instanceField = new InstanceField();
    var node = OBJECT_MAPPER.createObjectNode();
    var resource = new Resource().setDoc(node);
    var target = new Resource().setLabel("target");
    var edge = new ResourceEdge(resource, target, new Predicate("pred"));
    resource.setOutgoingEdges(Set.of(edge));

    // when
    coreMapper.mapWithResources(subResourceMapper, resource, instanceField::setInstance, Instance.class);

    // then
    assertThat(instanceField.getInstance(), notNullValue());
    verify(subResourceMapper).toDto(eq(edge), any(Instance.class));
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
    var node = getJsonNode(Map.of(NAME, names, QUALIFIER, qualifiers));
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
    var node = getJsonNode(Map.of(NAME, names, QUALIFIER, qualifiers));
    var resource = new Resource().setDoc(node).addType(new ResourceType());
    ObjectNode expectedNodeForHash = resource.getDoc().deepCopy();
    expectedNodeForHash.put(LABEL_RDF, resource.getLabel());
    expectedNodeForHash.put(TYPE, resource.getFirstType().getTypeHash());

    // when
    var result = coreMapper.hash(resource);

    // then
    assertThat(result, is(HashUtil.hash(expectedNodeForHash)));
  }

  @Test
  void hash_shouldReturnHashUtilResultForLabelAndType_ifGivenResourceContainsNoDoc() {
    // given
    var resource = new Resource().addType(new ResourceType());
    var expectedNodeForHash = OBJECT_MAPPER.createObjectNode();
    expectedNodeForHash.put(LABEL_RDF, resource.getLabel());
    expectedNodeForHash.put(TYPE, resource.getFirstType().getTypeHash());

    // when
    var result = coreMapper.hash(resource);

    // then
    assertThat(result, is(HashUtil.hash(expectedNodeForHash)));
  }

  @Test
  void hash_shouldReturnHashUtilResultForNodeOfEdgeJsons_ifGivenResourceContainsNoDoc() {
    // given
    var resource = new Resource().addType(new ResourceType());
    var targetNode1 = getObjectNode("label", "name", "link");
    var target1 = new Resource().setDoc(targetNode1).setResourceHash(111L).addType(new ResourceType());
    var predicate1 = new Predicate("predicate1");
    var targetNode2 = getObjectNode("label2", "name2", "link2");
    var target2 = new Resource().setDoc(targetNode2).setResourceHash(222L).addType(new ResourceType());
    var predicate2 = new Predicate("predicate2");
    var targetNode3 = getObjectNode("label3", "name3", "link3");
    var target3 = new Resource().setDoc(targetNode3).setResourceHash(333L).addType(new ResourceType());
    resource.getOutgoingEdges()
      .add(new ResourceEdge(resource, target1, predicate1));
    resource.getOutgoingEdges()
      .add(new ResourceEdge(resource, target2, predicate1));
    resource.getOutgoingEdges()
      .add(new ResourceEdge(resource, target3, predicate2));

    var expectedNodeForHash = OBJECT_MAPPER.createObjectNode();
    expectedNodeForHash.put(LABEL_RDF, resource.getLabel());
    expectedNodeForHash.put(TYPE, resource.getFirstType().getTypeHash());
    var arrayPredicate1 = OBJECT_MAPPER.createArrayNode();
    arrayPredicate1.add(targetNode1.deepCopy()
      .put(LABEL_RDF, target1.getLabel())
      .put(TYPE, target1.getFirstType().getTypeHash())
    );
    arrayPredicate1.add(targetNode2.deepCopy()
      .put(LABEL_RDF, target2.getLabel())
      .put(TYPE, target2.getFirstType().getTypeHash())
    );
    expectedNodeForHash.set(predicate1.getLabel(), arrayPredicate1);
    var arrayPredicate2 = OBJECT_MAPPER.createArrayNode();
    arrayPredicate2.add(targetNode3.deepCopy()
      .put(LABEL_RDF, target3.getLabel())
      .put(TYPE, target3.getFirstType().getTypeHash())
    );
    expectedNodeForHash.set(predicate2.getLabel(), arrayPredicate2);

    // when
    var result = coreMapper.hash(resource);

    // then
    assertThat(result, is(HashUtil.hash(expectedNodeForHash)));
  }

  @Test
  void hash_shouldReturnHashUtilResultForNodeOfDocAndEdgeJsons_ifGivenResourceContainsDocAndEdges() {
    // given
    var rootNode = getObjectNode("label", "name", "link");
    var resource = new Resource().setDoc(rootNode).addType(new ResourceType());
    var targetNode1 = getObjectNode("label2", "name2", "link2");
    var target1 = new Resource().setDoc(targetNode1).setResourceHash(111L).addType(new ResourceType());
    var predicate1 = new Predicate("predicate1");
    var targetNode2 = getObjectNode("label3", "name3", "link3");
    var target2 = new Resource().setDoc(targetNode2).setResourceHash(222L).addType(new ResourceType());
    var predicate2 = new Predicate("predicate2");
    var targetNode3 = getObjectNode("label4", "name4", "link4");
    var target3 = new Resource().setDoc(targetNode3).setResourceHash(333L).addType(new ResourceType());
    resource.getOutgoingEdges()
      .add(new ResourceEdge(resource, target1, predicate1));
    resource.getOutgoingEdges()
      .add(new ResourceEdge(resource, target2, predicate1));
    resource.getOutgoingEdges()
      .add(new ResourceEdge(resource, target3, predicate2));

    var expectedNodeForHash = rootNode.deepCopy();
    expectedNodeForHash.put(LABEL_RDF, resource.getLabel());
    expectedNodeForHash.put(TYPE, resource.getFirstType().getTypeHash());
    var arrayPredicate1 = OBJECT_MAPPER.createArrayNode();
    arrayPredicate1.add(targetNode1.deepCopy()
      .put(LABEL_RDF, target1.getLabel())
      .put(TYPE, target1.getFirstType().getTypeHash())
    );
    arrayPredicate1.add(targetNode2.deepCopy()
      .put(LABEL_RDF, target2.getLabel())
      .put(TYPE, target2.getFirstType().getTypeHash())
    );
    expectedNodeForHash.set(predicate1.getLabel(), arrayPredicate1);
    var arrayPredicate2 = OBJECT_MAPPER.createArrayNode();
    arrayPredicate2.add(targetNode3.deepCopy()
      .put(LABEL_RDF, target3.getLabel())
      .put(TYPE, target3.getFirstType().getTypeHash())
    );
    expectedNodeForHash.set(predicate2.getLabel(), arrayPredicate2);

    // when
    var result = coreMapper.hash(resource);

    // then
    assertThat(result, is(HashUtil.hash(expectedNodeForHash)));
  }

  @Test
  void toJson_shouldReturnCorrectJsonNodeFromString() throws JsonProcessingException {
    // given
    var json = getBibframeSample();

    // when
    var jsonNode = coreMapper.toJson(json);

    // then
    assertThat(OBJECT_MAPPER.writeValueAsString(jsonNode), equalToJson(getBibframeSample()));
  }

  @Test
  void toJson_shouldReturnCorrectJsonNodeFromMap() throws JsonProcessingException {
    // given
    var json = getBibframeSample();
    var map = OBJECT_MAPPER.readValue(json, new TypeReference<Map<String, Object>>() {
    });

    // when
    var jsonNode = coreMapper.toJson(map);

    // then
    assertThat(OBJECT_MAPPER.writeValueAsString(jsonNode), equalToJson(getBibframeSample()));
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
  void mapResourceEdges_shouldThrowNpe_ifGivenSourceIsNull(@Mock InnerResourceMapper mapper) {
    // given
    var dtoList = new ArrayList<>();
    Resource source = null;
    var predicate = "predicate";

    // when
    NullPointerException thrown = assertThrows(NullPointerException.class,
      () -> coreMapper.mapResourceEdges(dtoList, source, null, predicate, mapper::toEntity));

    // then
    assertThat(thrown.getMessage(), is("source is marked non-null but is null"));
  }

  @Test
  void mapResourceEdges_shouldThrowNpe_ifGivenPredicateIsNull(@Mock InnerResourceMapper mapper) {
    // given
    var dtoList = new ArrayList<>();
    var source = new Resource();
    String predicate = null;

    // when
    NullPointerException thrown = assertThrows(NullPointerException.class,
      () -> coreMapper.mapResourceEdges(dtoList, source, null, predicate, mapper::toEntity));

    // then
    assertThat(thrown.getMessage(), is("predicateLabel is marked non-null but is null"));
  }

  @Test
  void mapResourceEdges_shouldThrowNpe_ifGivenMappingFunctionIsNull() {
    // given
    var dtoList = new ArrayList<>();
    var source = new Resource();
    var predicate = "predicate";

    // when
    NullPointerException thrown = assertThrows(NullPointerException.class,
      () -> coreMapper.mapResourceEdges(dtoList, source, null, predicate, null));

    // then
    assertThat(thrown.getMessage(), is("mappingFunction is marked non-null but is null"));
  }

  @Test
  void mapResourceEdges_shouldDoNothing_ifGivenDtoListIsNull(@Mock InnerResourceMapper mapper) {
    // given
    List dtoList = null;
    var source = new Resource();
    var predicate = "predicate";

    // when
    coreMapper.mapResourceEdges(dtoList, source, null, predicate, mapper::toEntity);

    // then
    verify(mapper, never()).toEntity(any(), any());
    assertThat(source.getOutgoingEdges(), hasSize(0));
  }

  @Test
  void mapResourceEdges_shouldDoNothing_ifGivenDtoListIsEmpty(@Mock InnerResourceMapper mapper) {
    // given
    var dtoList = new ArrayList<>();
    var source = new Resource();
    var predicate = "predicate";

    // when
    coreMapper.mapResourceEdges(dtoList, source, null, predicate, mapper::toEntity);

    // then
    verify(mapper, never()).toEntity(any(), any());
    assertThat(source.getOutgoingEdges(), hasSize(0));
  }

  @Test
  void mapResourceEdges_shouldAddMappedEdgesToResource_ifGivenDtoListIsNotEmptyAndNoType(
    @Mock InnerResourceMapper mapper) {
    // given
    var dto1 = new Isbn().id(randomLong().toString());
    var dto2 = new Isbn().id(randomLong().toString());
    var predicate = "predicate";
    var expectedPredicate = new Predicate(predicate);
    doReturn(expectedPredicate).when(predicateService).get(predicate);
    var expectedTarget1 = new Resource().setLabel("expectedTarget1").setResourceHash(111L);
    doReturn(expectedTarget1).when(mapper).toEntity(dto1, predicate);
    var expectedTarget2 = new Resource().setLabel("expectedTarget2").setResourceHash(222L);
    doReturn(expectedTarget2).when(mapper).toEntity(dto2, predicate);
    var source = new Resource();
    var dtoList = List.of(dto1, dto2);

    // when
    coreMapper.mapResourceEdges(dtoList, source, null, predicate, mapper::toEntity);

    // then
    assertThat(source.getOutgoingEdges(), hasSize(2));
    var edgesAreExpected = source.getOutgoingEdges().stream().allMatch(edge ->
      edge.getPredicate().equals(expectedPredicate)
        && edge.getSource().equals(source)
        && (edge.getTarget().equals(expectedTarget1)) || edge.getTarget().equals(expectedTarget2));
    assertThat(edgesAreExpected, is(true));
  }

  @Test
  void mapResourceEdges_shouldAddMappedEdgesToResource_ifGivenDtoListIsNotEmptyAndType(
    @Mock InnerResourceMapper mapper) {
    // given
    var dto1 = new Isbn().id(randomLong().toString());
    var dto2 = new Isbn().id(randomLong().toString());
    var predicate = "predicate";
    var expectedPredicate = new Predicate(predicate);
    doReturn(expectedPredicate).when(predicateService).get(predicate);
    var type = TYPE;
    var expectedTarget1 = new Resource().setLabel("expectedTarget1").setResourceHash(111L);
    doReturn(expectedTarget1).when(mapper).toEntity(dto1, type);
    var expectedTarget2 = new Resource().setLabel("expectedTarget2").setResourceHash(222L);
    doReturn(expectedTarget2).when(mapper).toEntity(dto2, type);
    var source = new Resource();
    var dtoList = List.of(dto1, dto2);

    // when
    coreMapper.mapResourceEdges(dtoList, source, type, predicate, mapper::toEntity);

    // then
    assertThat(source.getOutgoingEdges(), hasSize(2));
    var edgesAreExpected = source.getOutgoingEdges().stream().allMatch(edge ->
      edge.getPredicate().equals(expectedPredicate)
        && edge.getSource().equals(source)
        && (edge.getTarget().equals(expectedTarget1)) || edge.getTarget().equals(expectedTarget2));
    assertThat(edgesAreExpected, is(true));
  }

  @Test
  void mapResourceEdges2_shouldThrowNpe_ifGivenSourceIsNull(@Mock SubResourceMapper mapper) {
    // given
    var dtoList = new ArrayList<>();
    Resource source = null;
    var predicate = "predicate";
    var parent = Instance.class;

    // when
    NullPointerException thrown = assertThrows(NullPointerException.class,
      () -> coreMapper.mapResourceEdges(dtoList, source, predicate, parent, mapper::toEntity));

    // then
    assertThat(thrown.getMessage(), is("source is marked non-null but is null"));
  }

  @Test
  void mapResourceEdges2_shouldThrowNpe_ifGivenPredicateIsNull(@Mock SubResourceMapper mapper) {
    // given
    var dtoList = new ArrayList<>();
    var source = new Resource();
    String predicate = null;
    var parent = Instance.class;

    // when
    NullPointerException thrown = assertThrows(NullPointerException.class,
      () -> coreMapper.mapResourceEdges(dtoList, source, predicate, parent, mapper::toEntity));

    // then
    assertThat(thrown.getMessage(), is("predicateLabel is marked non-null but is null"));
  }

  @Test
  void mapResourceEdges2_shouldThrowNpe_ifGivenParentIsNull(@Mock SubResourceMapper mapper) {
    // given
    var dtoList = new ArrayList<>();
    var source = new Resource();
    var predicate = "predicate";
    Class<Object> parent = null;

    // when
    NullPointerException thrown = assertThrows(NullPointerException.class,
      () -> coreMapper.mapResourceEdges(dtoList, source, predicate, parent, mapper::toEntity));

    // then
    assertThat(thrown.getMessage(), is("parent is marked non-null but is null"));
  }

  @Test
  void mapResourceEdges2_shouldThrowNpe_ifGivenMappingFunctionIsNull() {
    // given
    var dtoList = new ArrayList<>();
    var source = new Resource();
    var predicate = "predicate";
    var parent = Instance.class;

    // when
    NullPointerException thrown = assertThrows(NullPointerException.class,
      () -> coreMapper.mapResourceEdges(dtoList, source, predicate, parent, null));

    // then
    assertThat(thrown.getMessage(), is("mapping is marked non-null but is null"));
  }

  @Test
  void mapResourceEdges2_shouldDoNothing_ifGivenDtoListIsNull(@Mock SubResourceMapper mapper) {
    // given
    List dtoList = null;
    var source = new Resource();
    var predicate = "predicate";
    var parent = Instance.class;

    // when
    coreMapper.mapResourceEdges(dtoList, source, predicate, parent, mapper::toEntity);

    // then
    verify(mapper, never()).toEntity(any(), any(), any());
    assertThat(source.getOutgoingEdges(), hasSize(0));
  }

  @Test
  void mapResourceEdges2_shouldDoNothing_ifGivenDtoListIsEmpty(@Mock SubResourceMapper mapper) {
    // given
    var dtoList = new ArrayList<>();
    var source = new Resource();
    var predicate = "predicate";
    var parent = Instance.class;

    // when
    coreMapper.mapResourceEdges(dtoList, source, predicate, parent, mapper::toEntity);

    // then
    verify(mapper, never()).toEntity(any(), any(), any());
    assertThat(source.getOutgoingEdges(), hasSize(0));
  }

  @Test
  void mapResourceEdges2_shouldAddMappedEdgesToResource_ifGivenDtoListIsNotEmpty(
    @Mock SubResourceMapper mapper) {
    // given
    var dto1 = new Isbn().id(randomLong().toString());
    var dto2 = new Isbn().id(randomLong().toString());
    var predicate = "predicate";
    var parent = Instance.class;
    var expectedPredicate = new Predicate(predicate);
    doReturn(expectedPredicate).when(predicateService).get(predicate);
    var expectedTarget1 = new Resource().setLabel("expectedTarget1").setResourceHash(111L);
    doReturn(expectedTarget1).when(mapper).toEntity(dto1, predicate, parent);
    var expectedTarget2 = new Resource().setLabel("expectedTarget2").setResourceHash(222L);
    doReturn(expectedTarget2).when(mapper).toEntity(dto2, predicate, parent);
    var source = new Resource();
    var dtoList = List.of(dto1, dto2);

    // when
    coreMapper.mapResourceEdges(dtoList, source, predicate, parent, mapper::toEntity);

    // then
    assertThat(source.getOutgoingEdges(), hasSize(2));
    var edgesAreExpected = source.getOutgoingEdges().stream().allMatch(edge ->
      edge.getPredicate().equals(expectedPredicate)
        && edge.getSource().equals(source)
        && (edge.getTarget().equals(expectedTarget1)) || edge.getTarget().equals(expectedTarget2));
    assertThat(edgesAreExpected, is(true));
  }

}
