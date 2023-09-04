package org.folio.linked.data.mapper.resource.common;

import static org.folio.linked.data.test.IsEqualJson.equalToJson;
import static org.folio.linked.data.test.TestUtil.OBJECT_MAPPER;
import static org.folio.linked.data.test.TestUtil.getBibframe2Sample;
import static org.folio.linked.data.test.TestUtil.getJsonNode;
import static org.folio.linked.data.test.TestUtil.getPropertyNode;
import static org.folio.linked.data.test.TestUtil.propertyToDoc;
import static org.folio.linked.data.test.TestUtil.provisionActivityToDoc;
import static org.folio.linked.data.test.TestUtil.random;
import static org.folio.linked.data.util.Bibframe2Constants.DATE_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.PLACE2_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.PLACE_COMPONENTS;
import static org.folio.linked.data.util.Bibframe2Constants.PROPERTY_LABEL;
import static org.folio.linked.data.util.Bibframe2Constants.SIMPLE_AGENT_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.SIMPLE_DATE_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.SIMPLE_PLACE_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.VALUE_PRED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
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
import java.util.function.Consumer;
import org.folio.linked.data.domain.dto.Bibframe2Response;
import org.folio.linked.data.domain.dto.Extent2;
import org.folio.linked.data.domain.dto.ImmediateAcquisition2;
import org.folio.linked.data.domain.dto.Instance2;
import org.folio.linked.data.domain.dto.Property2;
import org.folio.linked.data.domain.dto.ProvisionActivity2;
import org.folio.linked.data.domain.dto.Url2;
import org.folio.linked.data.mapper.resource.common.inner.InnerResourceMapper;
import org.folio.linked.data.mapper.resource.common.inner.sub.SubResourceMapper;
import org.folio.linked.data.mapper.resource.common.inner.sub.SubResourceMapperUnit;
import org.folio.linked.data.mapper.resource.monograph.inner.common.NoteMapperUnit;
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
  void toProperty_shouldThrowNpe_ifGivenResourceIsNull() {
    // given
    Resource resource = null;

    // when
    NullPointerException thrown = assertThrows(NullPointerException.class, () -> coreMapper.toProperty(resource));

    // then
    assertThat(thrown.getMessage(), is("resource is marked non-null but is null"));
  }

  @Test
  void toProperty_shouldReturnEmptyProperty_ifGivenResourceContainsEmptyDoc() {
    // given
    var resource = new Resource();

    // when
    var property = coreMapper.toProperty(resource);

    // then
    assertThat(property, is(new Property2()));
  }

  @Test
  void toProperty_shouldReturnCorrectProperty_ifGivenResourceContainsCorrectDoc() {
    // given
    var node = getPropertyNode("id", "label", "uri");
    var resource = new Resource().setDoc(node);

    // when
    var property = coreMapper.toProperty(resource);

    // then
    assertThat(property.getId(), is("id"));
    assertThat(property.getLabel(), is("label"));
    assertThat(property.getUri(), is("uri"));
  }

  @Test
  void toProvisionActivity_shouldThrowNpe_ifGivenResourceIsNull() {
    // given
    Resource resource = null;

    // when
    NullPointerException thrown = assertThrows(NullPointerException.class,
      () -> coreMapper.toProvisionActivity(resource));

    // then
    assertThat(thrown.getMessage(), is("resource is marked non-null but is null"));
  }

  @Test
  void toProvisionActivity_shouldReturnEmptyProperty_ifGivenResourceContainsEmptyDoc() {
    // given
    var resource = new Resource();

    // when
    var provisionActivity = coreMapper.toProvisionActivity(resource);

    // then
    assertThat(provisionActivity, is(new ProvisionActivity2()));
  }

  @Test
  void toProvisionActivity_shouldReturnCorrectProperty_ifGivenResourceContainsCorrectDoc() {
    // given
    var map = Map.of(
      DATE_PRED, List.of("1900", "1986"),
      SIMPLE_DATE_PRED, List.of("2001", "2023"),
      SIMPLE_AGENT_PRED, List.of("Charles Scribner's Sons", "Agent 2"),
      SIMPLE_PLACE_PRED, List.of("New York", "Karaganda")
    );
    var node = getJsonNode(map);
    var resource = new Resource().setDoc(node);

    // when
    var provisionActivity = coreMapper.toProvisionActivity(resource);

    // then
    assertThat(provisionActivity.getDate().size(), is(2));
    assertThat(provisionActivity.getDate().get(0), is("1900"));
    assertThat(provisionActivity.getDate().get(1), is("1986"));
    assertThat(provisionActivity.getSimpleAgent().size(), is(2));
    assertThat(provisionActivity.getSimpleAgent().get(0), is("Charles Scribner's Sons"));
    assertThat(provisionActivity.getSimpleAgent().get(1), is("Agent 2"));
    assertThat(provisionActivity.getSimpleDate().size(), is(2));
    assertThat(provisionActivity.getSimpleDate().get(0), is("2001"));
    assertThat(provisionActivity.getSimpleDate().get(1), is("2023"));
    assertThat(provisionActivity.getSimplePlace().size(), is(2));
    assertThat(provisionActivity.getSimplePlace().get(0), is("New York"));
    assertThat(provisionActivity.getSimplePlace().get(1), is("Karaganda"));
    assertThat(provisionActivity.getPlace(), nullValue());
  }

  @Test
  void toUrl_shouldThrowNpe_ifGivenResourceIsNull() {
    // given
    Resource resource = null;

    // when
    NullPointerException thrown = assertThrows(NullPointerException.class, () -> coreMapper.toUrl(resource));

    // then
    assertThat(thrown.getMessage(), is("resource is marked non-null but is null"));
  }

  @Test
  void toUrl_shouldReturnEmptyProperty_ifGivenResourceContainsEmptyDoc() {
    // given
    var resource = new Resource();

    // when
    var url = coreMapper.toUrl(resource);

    // then
    assertThat(url, is(new Url2()));
  }

  @Test
  void toUrl_shouldReturnCorrectProperty_ifGivenResourceContainsCorrectDoc() {
    // given
    var map = Map.of(
      VALUE_PRED, List.of("value", "value2")
    );
    var node = getJsonNode(map);
    var resource = new Resource().setDoc(node);

    // when
    var url = coreMapper.toUrl(resource);

    // then
    assertThat(url.getValue().size(), is(2));
    assertThat(url.getValue().get(0), is("value"));
    assertThat(url.getValue().get(1), is("value2"));
    assertThat(url.getNote(), nullValue());
  }

  @Test
  void addMappedResources_shouldThrowNpe_ifGivenMapperIsNull(@Mock Resource source) {
    // given
    SubResourceMapperUnit subResourceMapperUnit = null;
    String predicate = null;
    var destination = new Extent2();

    // when
    NullPointerException thrown = assertThrows(NullPointerException.class,
      () -> coreMapper.addMappedResources(subResourceMapperUnit, source, predicate, destination));

    // then
    assertThat(thrown.getMessage(), is("subResourceMapperUnit is marked non-null but is null"));
  }

  @Test
  void addMappedResources_shouldThrowNpe_ifGivenSourceIsNull(@Mock NoteMapperUnit subResourceMapperUnit) {
    // given
    Resource source = null;
    var predicate = "predicate";
    var destination = new Extent2();

    // when
    NullPointerException thrown = assertThrows(NullPointerException.class,
      () -> coreMapper.addMappedResources(subResourceMapperUnit, source, predicate, destination));

    // then
    assertThat(thrown.getMessage(), is("source is marked non-null but is null"));
  }

  @Test
  void addMappedResources_shouldThrowNpe_ifGivenPredicateIsNull(@Mock NoteMapperUnit subResourceMapperUnit,
                                                                @Mock Resource source) {
    // given
    String predicate = null;
    var destination = new Extent2();

    // when
    NullPointerException thrown = assertThrows(NullPointerException.class,
      () -> coreMapper.addMappedResources(subResourceMapperUnit, source, predicate, destination));

    // then
    assertThat(thrown.getMessage(), is("predicate is marked non-null but is null"));
  }

  @Test
  void addMappedResources_shouldThrowNpe_ifGivenDestinationIsNull(@Mock NoteMapperUnit subResourceMapperUnit,
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
    @Mock NoteMapperUnit<Extent2> subResourceMapperUnit) {
    // given
    var predicate = "predicate";
    var targetResource = new Resource().setLabel("target");
    var notTargetResource = new Resource().setLabel("notTarget");
    var source = new Resource().setLabel("source");
    source.getOutgoingEdges()
      .add(new ResourceEdge(source, targetResource, new Predicate(predicate)));
    source.getOutgoingEdges()
      .add(new ResourceEdge(source, notTargetResource, new Predicate("not " + predicate)));
    var destination = new Extent2();

    // when
    coreMapper.addMappedResources(subResourceMapperUnit, source, predicate, destination);

    // then
    verify(subResourceMapperUnit).toDto(targetResource, destination);
  }

  @Test
  void mapWithResources_shouldThrowNpe_ifGivenSubResourceMapperIsNull(@Mock Resource resource,
                                                                      @Mock Consumer<Instance2> consumer) {
    // given
    SubResourceMapper subResourceMapper = null;

    // when
    NullPointerException thrown = assertThrows(NullPointerException.class,
      () -> coreMapper.mapWithResources(subResourceMapper, resource, consumer, Instance2.class));

    // then
    assertThat(thrown.getMessage(), is("subResourceMapper is marked non-null but is null"));
  }

  @Test
  void mapWithResources_shouldThrowNpe_ifGivenResourceIsNull(@Mock SubResourceMapper subResourceMapper,
                                                             @Mock Consumer<Instance2> consumer) {
    // given
    Resource resource = null;

    // when
    NullPointerException thrown = assertThrows(NullPointerException.class,
      () -> coreMapper.mapWithResources(subResourceMapper, resource, consumer, Instance2.class));

    // then
    assertThat(thrown.getMessage(), is("resource is marked non-null but is null"));
  }

  @Test
  void mapWithResources_shouldThrowNpe_ifGivenConsumerIsNull(@Mock SubResourceMapper subResourceMapper,
                                                             @Mock Resource resource) {
    // given
    Consumer<Instance2> consumer = null;

    // when
    NullPointerException thrown = assertThrows(NullPointerException.class,
      () -> coreMapper.mapWithResources(subResourceMapper, resource, consumer, Instance2.class));

    // then
    assertThat(thrown.getMessage(), is("consumer is marked non-null but is null"));
  }

  @Test
  void mapWithResources_shouldThrowNpe_ifGivenDestinationIsNull(@Mock SubResourceMapper subResourceMapper,
                                                                @Mock Consumer<Instance2> consumer,
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
    var bibframeResponse = new Bibframe2Response();
    var resource = new Resource();

    // when
    coreMapper.mapWithResources(subResourceMapper, resource, bibframeResponse::addInstanceItem, Instance2.class);

    // then
    assertThat(bibframeResponse.getInstance(), hasSize(1));
  }

  @Test
  void mapWithResources_shouldAddDestinationObjectToGivenConsumer_ifGivenResourceContainsDoc(
    @Mock SubResourceMapper subResourceMapper) {
    // given
    var bibframeResponse = new Bibframe2Response();
    var node = OBJECT_MAPPER.createObjectNode();
    var resource = new Resource().setDoc(node);

    // when
    coreMapper.mapWithResources(subResourceMapper, resource, bibframeResponse::addInstanceItem, Instance2.class);

    // then
    assertThat(bibframeResponse.getInstance(), hasSize(1));
  }

  @Test
  void mapWithResources_shouldAddDestinationObjectToGivenConsumerAndMapEdge_ifGivenResourceContainsDocAndEdge(
    @Mock SubResourceMapper subResourceMapper) {
    // given
    var bibframeResponse = new Bibframe2Response();
    var node = OBJECT_MAPPER.createObjectNode();
    var resource = new Resource().setDoc(node);
    var target = new Resource().setLabel("target");
    var edge = new ResourceEdge(resource, target, new Predicate("pred"));
    resource.setOutgoingEdges(Set.of(edge));

    // when
    coreMapper.mapWithResources(subResourceMapper, resource, bibframeResponse::addInstanceItem, Instance2.class);

    // then
    assertThat(bibframeResponse.getInstance(), hasSize(1));
    verify(subResourceMapper).toDto(eq(edge), any(Instance2.class));
  }

  @Test
  void addMappedProperties_shouldThrowNpe_ifGivenResourceIsNull(@Mock Consumer<Property2> consumer) {
    // given
    Resource resource = null;
    var predicate = "predicate";

    // when
    NullPointerException thrown = assertThrows(NullPointerException.class,
      () -> coreMapper.addMappedProperties(resource, predicate, consumer));

    // then
    assertThat(thrown.getMessage(), is("resource is marked non-null but is null"));
  }

  @Test
  void addMappedProperties_shouldThrowNpe_ifGivenPredicateIsNull(@Mock Consumer<Property2> consumer) {
    // given
    var resource = new Resource();
    String predicate = null;

    // when
    NullPointerException thrown = assertThrows(NullPointerException.class,
      () -> coreMapper.addMappedProperties(resource, predicate, consumer));

    // then
    assertThat(thrown.getMessage(), is("predicate is marked non-null but is null"));
  }

  @Test
  void addMappedProperties_shouldThrowNpe_ifGivenConsumerIsNull() {
    // given
    var resource = new Resource();
    var predicate = "predicate";
    Consumer<Property2> consumer = null;

    // when
    NullPointerException thrown = assertThrows(NullPointerException.class,
      () -> coreMapper.addMappedProperties(resource, predicate, consumer));

    // then
    assertThat(thrown.getMessage(), is("consumer is marked non-null but is null"));
  }

  @Test
  void addMappedProperties_shouldDoNothing_ifGivenResourceHasNoEdges() {
    // given
    var resource = new Resource();
    var predicate = "predicate";
    var immediateAcquisition = new ImmediateAcquisition2();

    // when
    coreMapper.addMappedProperties(resource, predicate, immediateAcquisition::addApplicableInstitutionItem);

    // then
    assertThat(immediateAcquisition.getApplicableInstitution(), nullValue());
  }

  @Test
  void addMappedProperties_shouldDoNothing_ifGivenResourceHasEdgeWithNotExpectedPredicate() {
    // given
    var resource = new Resource();
    resource.setOutgoingEdges(
      Set.of(new ResourceEdge(resource, new Resource(), new Predicate("notExpectedPredicate"))));
    var predicate = "predicate";
    var immediateAcquisition = new ImmediateAcquisition2();

    // when
    coreMapper.addMappedProperties(resource, predicate, immediateAcquisition::addApplicableInstitutionItem);

    // then
    assertThat(immediateAcquisition.getApplicableInstitution(), nullValue());
  }

  @Test
  void addMappedProperties_shouldDoNothing_ifGivenResourceHasEdgeWithExpectedPredicateButNoDoc() {
    // given
    var resource = new Resource();
    var predicate = "predicate";
    var target = new Resource();
    resource.setOutgoingEdges(Set.of(new ResourceEdge(resource, target, new Predicate(predicate))));

    var immediateAcquisition = new ImmediateAcquisition2();

    // when
    coreMapper.addMappedProperties(resource, predicate, immediateAcquisition::addApplicableInstitutionItem);

    // then
    assertThat(immediateAcquisition.getApplicableInstitution(), nullValue());
  }

  @Test
  void addMappedProperties_shouldPassMappedPropertyToGivenConsumer_ifGivenResourceHasExpectedEdge() {
    // given
    var resource = new Resource();
    var predicate = "predicate";
    var target = new Resource().setDoc(getPropertyNode("id", "label", "uri"));
    resource.setOutgoingEdges(Set.of(new ResourceEdge(resource, target, new Predicate(predicate))));

    var immediateAcquisition = new ImmediateAcquisition2();

    // when
    coreMapper.addMappedProperties(resource, predicate, immediateAcquisition::addApplicableInstitutionItem);

    // then
    assertThat(immediateAcquisition.getApplicableInstitution(), hasSize(1));
    assertThat(immediateAcquisition.getApplicableInstitution().get(0).getId(), is("id"));
    assertThat(immediateAcquisition.getApplicableInstitution().get(0).getLabel(), is("label"));
    assertThat(immediateAcquisition.getApplicableInstitution().get(0).getUri(), is("uri"));
  }

  @Test
  void readResourceDoc_shouldThrowNpe_ifGivenResourceIsNull() {
    // given
    Resource resource = null;
    var dtoClass = Instance2.class;

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
    var dtoClass = Property2.class;

    // when
    var result = coreMapper.readResourceDoc(resource, dtoClass);

    // then
    assertThat(result, is(new Property2()));
  }

  @Test
  void readResourceDoc_shouldReturnCorrectDto_ifGivenResourceHasDoc() {
    // given
    var node = getPropertyNode("id", "label", "uri");
    var resource = new Resource().setDoc(node);
    var dtoClass = Property2.class;

    // when
    var result = coreMapper.readResourceDoc(resource, dtoClass);

    // then
    assertThat(result.getId(), is("id"));
    assertThat(result.getLabel(), is("label"));
    assertThat(result.getUri(), is("uri"));
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
    var node = getPropertyNode("id", "label", "uri");
    var resource = new Resource().setDoc(node).setType(new ResourceType());
    ObjectNode expectedNodeForHash = resource.getDoc().deepCopy();
    expectedNodeForHash.put(PROPERTY_LABEL, resource.getLabel());
    expectedNodeForHash.put("type", resource.getType().getTypeHash());

    // when
    var result = coreMapper.hash(resource);

    // then
    assertThat(result, is(HashUtil.hash(expectedNodeForHash)));
  }

  @Test
  void hash_shouldReturnHashUtilResultForLabelAndType_ifGivenResourceContainsNoDoc() {
    // given
    var resource = new Resource().setType(new ResourceType());
    ObjectNode expectedNodeForHash = OBJECT_MAPPER.createObjectNode();
    expectedNodeForHash.put(PROPERTY_LABEL, resource.getLabel());
    expectedNodeForHash.put("type", resource.getType().getTypeHash());

    // when
    var result = coreMapper.hash(resource);

    // then
    assertThat(result, is(HashUtil.hash(expectedNodeForHash)));
  }

  @Test
  void hash_shouldReturnHashUtilResultForNodeOfEdgeJsons_ifGivenResourceContainsNoDoc() {
    // given
    var resource = new Resource().setType(new ResourceType());
    var targetNode1 = getPropertyNode("id", "label", "uri");
    var target1 = new Resource().setDoc(targetNode1).setResourceHash(111L).setType(new ResourceType());
    var predicate1 = new Predicate("predicate1");
    var targetNode2 = getPropertyNode("id2", "label2", "uri2");
    var target2 = new Resource().setDoc(targetNode2).setResourceHash(222L).setType(new ResourceType());
    var predicate2 = new Predicate("predicate2");
    var targetNode3 = getPropertyNode("id3", "label3", "uri3");
    var target3 = new Resource().setDoc(targetNode3).setResourceHash(333L).setType(new ResourceType());
    resource.getOutgoingEdges()
      .add(new ResourceEdge(resource, target1, predicate1));
    resource.getOutgoingEdges()
      .add(new ResourceEdge(resource, target2, predicate1));
    resource.getOutgoingEdges()
      .add(new ResourceEdge(resource, target3, predicate2));

    var expectedNodeForHash = OBJECT_MAPPER.createObjectNode();
    expectedNodeForHash.put(PROPERTY_LABEL, resource.getLabel());
    expectedNodeForHash.put("type", resource.getType().getTypeHash());
    var arrayPredicate1 = OBJECT_MAPPER.createArrayNode();
    arrayPredicate1.add(targetNode1.deepCopy()
      .put(PROPERTY_LABEL, target1.getLabel())
      .put("type", target1.getType().getTypeHash())
    );
    arrayPredicate1.add(targetNode2.deepCopy()
      .put(PROPERTY_LABEL, target2.getLabel())
      .put("type", target2.getType().getTypeHash())
    );
    expectedNodeForHash.set(predicate1.getLabel(), arrayPredicate1);
    var arrayPredicate2 = OBJECT_MAPPER.createArrayNode();
    arrayPredicate2.add(targetNode3.deepCopy()
      .put(PROPERTY_LABEL, target3.getLabel())
      .put("type", target3.getType().getTypeHash())
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
    var rootNode = getPropertyNode("rootId", "rootLabel", "rootUri");
    var resource = new Resource().setDoc(rootNode).setType(new ResourceType());
    var targetNode1 = getPropertyNode("id", "label", "uri");
    var target1 = new Resource().setDoc(targetNode1).setResourceHash(111L).setType(new ResourceType());
    var predicate1 = new Predicate("predicate1");
    var targetNode2 = getPropertyNode("id2", "label2", "uri2");
    var target2 = new Resource().setDoc(targetNode2).setResourceHash(222L).setType(new ResourceType());
    var predicate2 = new Predicate("predicate2");
    var targetNode3 = getPropertyNode("id3", "label3", "uri3");
    var target3 = new Resource().setDoc(targetNode3).setResourceHash(333L).setType(new ResourceType());
    resource.getOutgoingEdges()
      .add(new ResourceEdge(resource, target1, predicate1));
    resource.getOutgoingEdges()
      .add(new ResourceEdge(resource, target2, predicate1));
    resource.getOutgoingEdges()
      .add(new ResourceEdge(resource, target3, predicate2));

    var expectedNodeForHash = rootNode.deepCopy();
    expectedNodeForHash.put(PROPERTY_LABEL, resource.getLabel());
    expectedNodeForHash.put("type", resource.getType().getTypeHash());
    var arrayPredicate1 = OBJECT_MAPPER.createArrayNode();
    arrayPredicate1.add(targetNode1.deepCopy()
      .put(PROPERTY_LABEL, target1.getLabel())
      .put("type", target1.getType().getTypeHash())
    );
    arrayPredicate1.add(targetNode2.deepCopy()
      .put(PROPERTY_LABEL, target2.getLabel())
      .put("type", target2.getType().getTypeHash())
    );
    expectedNodeForHash.set(predicate1.getLabel(), arrayPredicate1);
    var arrayPredicate2 = OBJECT_MAPPER.createArrayNode();
    arrayPredicate2.add(targetNode3.deepCopy()
      .put(PROPERTY_LABEL, target3.getLabel())
      .put("type", target3.getType().getTypeHash())
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
    var json = getBibframe2Sample();

    // when
    var jsonNode = coreMapper.toJson(json);

    // then
    assertThat(OBJECT_MAPPER.writeValueAsString(jsonNode), equalToJson(getBibframe2Sample()));
  }

  @Test
  void toJson_shouldReturnCorrectJsonNodeFromMap() throws JsonProcessingException {
    // given
    var json = getBibframe2Sample();
    var map = OBJECT_MAPPER.readValue(json, new TypeReference<Map<String, Object>>() {
    });

    // when
    var jsonNode = coreMapper.toJson(map);

    // then
    assertThat(OBJECT_MAPPER.writeValueAsString(jsonNode), equalToJson(getBibframe2Sample()));
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
    var dto1 = new Property2().id("id").label("label").uri("uri");
    var dto2 = new Property2().id("id2").label("label2").uri("uri2");
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
    var dto1 = new Property2().id("id").label("label").uri("uri");
    var dto2 = new Property2().id("id2").label("label2").uri("uri2");
    var predicate = "predicate";
    var expectedPredicate = new Predicate(predicate);
    doReturn(expectedPredicate).when(predicateService).get(predicate);
    var type = "type";
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
    var parent = Instance2.class;

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
    var parent = Instance2.class;

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
    var parent = Instance2.class;

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
    var parent = Instance2.class;

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
    var parent = Instance2.class;

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
    var dto1 = new Property2().id("id").label("label").uri("uri");
    var dto2 = new Property2().id("id2").label("label2").uri("uri2");
    var predicate = "predicate";
    var parent = Instance2.class;
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

  @Test
  void mapPropertyEdges_shouldThrowNpe_ifGivenSourceIsNull() {
    // given
    var subProperties = new ArrayList<Property2>();
    Resource source = null;
    var predicate = "predicate";
    var type = "type";

    // when
    NullPointerException thrown = assertThrows(NullPointerException.class,
      () -> coreMapper.mapPropertyEdges(subProperties, source, predicate, type));

    // then
    assertThat(thrown.getMessage(), is("source is marked non-null but is null"));
  }

  @Test
  void mapPropertyEdges_shouldThrowNpe_ifGivenPredicateIsNull() {
    // given
    var subProperties = new ArrayList<Property2>();
    var source = new Resource();
    String predicate = null;
    var type = "type";

    // when
    NullPointerException thrown = assertThrows(NullPointerException.class,
      () -> coreMapper.mapPropertyEdges(subProperties, source, predicate, type));

    // then
    assertThat(thrown.getMessage(), is("predicateLabel is marked non-null but is null"));
  }

  @Test
  void mapPropertyEdges_shouldThrowNpe_ifGivenTypeIsNull() {
    // given
    var subProperties = new ArrayList<Property2>();
    var source = new Resource();
    var predicate = "predicate";
    String type = null;

    // when
    NullPointerException thrown = assertThrows(NullPointerException.class,
      () -> coreMapper.mapPropertyEdges(subProperties, source, predicate, type));

    // then
    assertThat(thrown.getMessage(), is("resourceType is marked non-null but is null"));
  }

  @Test
  void mapPropertyEdges_shouldDoNothing_ifGivenDtoListIsNull() {
    // given
    List<Property2> subProperties = null;
    var source = new Resource();
    var predicate = "predicate";
    var type = "type";

    // when
    coreMapper.mapPropertyEdges(subProperties, source, predicate, type);

    // then
    assertThat(source.getOutgoingEdges(), hasSize(0));
  }

  @Test
  void mapPropertyEdges_shouldDoNothing_ifGivenDtoListIsEmpty() {
    // given
    var subProperties = new ArrayList<Property2>();
    var source = new Resource();
    var predicate = "predicate";
    var type = "type";

    // when
    coreMapper.mapPropertyEdges(subProperties, source, predicate, type);

    // then
    assertThat(source.getOutgoingEdges(), hasSize(0));
  }

  @Test
  void mapPropertyEdges_shouldAddMappedEdgesToResource_ifGivenDtoListIsNotEmpty() {
    // given
    var dto1 = new Property2().id("id").label("label").uri("uri");
    var predicate = "predicate";
    var expectedPredicate = new Predicate(predicate);
    doReturn(expectedPredicate).when(predicateService).get(predicate);
    var type = "type";
    var expectedType = new ResourceType().setSimpleLabel(type);
    doReturn(expectedType).when(resourceTypeService).get(type);
    var expectedTarget1 = new Resource()
      .setLabel(dto1.getLabel())
      .setType(expectedType)
      .setDoc(propertyToDoc(dto1));
    expectedTarget1.setResourceHash(coreMapper.hash(expectedTarget1));
    var dto2 = new Property2().id("id2").label("label2").uri("uri2");
    var expectedTarget2 = new Resource()
      .setLabel(dto2.getLabel())
      .setType(expectedType)
      .setDoc(propertyToDoc(dto2));
    expectedTarget2.setResourceHash(coreMapper.hash(expectedTarget2));
    var subProperties = List.of(dto1, dto2);
    var source = new Resource();

    // when
    coreMapper.mapPropertyEdges(subProperties, source, predicate, type);

    // then
    assertThat(source.getOutgoingEdges(), hasSize(2));
    var edgesAreExpected = source.getOutgoingEdges().stream().allMatch(edge ->
      edge.getPredicate().equals(expectedPredicate)
        && edge.getSource().equals(source)
        && (edge.getTarget().equals(expectedTarget1)) || edge.getTarget().equals(expectedTarget2));
    assertThat(edgesAreExpected, is(true));
  }

  @Test
  void propertyToEntity_shouldThrowNpe_ifGivenPropertyIsNull() {
    // given
    Property2 property = null;
    var type = "type";

    // when
    NullPointerException thrown = assertThrows(NullPointerException.class,
      () -> coreMapper.propertyToEntity(property, type));

    // then
    assertThat(thrown.getMessage(), is("property is marked non-null but is null"));
  }

  @Test
  void propertyToEntity_shouldThrowNpe_ifGivenResourceTypeIsNull() {
    // given
    var property = new Property2();
    String type = null;

    // when
    NullPointerException thrown = assertThrows(NullPointerException.class,
      () -> coreMapper.propertyToEntity(property, type));

    // then
    assertThat(thrown.getMessage(), is("resourceType is marked non-null but is null"));
  }

  @Test
  void propertyToEntity_shouldReturnCorrectEntity_ifGivenPropertyAndTypeNotNull() {
    // given
    var property = random(Property2.class);
    var type = "type";
    var expectedType = new ResourceType().setSimpleLabel(type);
    doReturn(expectedType).when(resourceTypeService).get(type);

    // when
    var resource = coreMapper.propertyToEntity(property, type);

    // then
    assertThat(resource.getLabel(), is(property.getLabel()));
    assertThat(resource.getType(), is(expectedType));
    assertThat(resource.getDoc(), is(propertyToDoc(property)));
    assertThat(resource.getResourceHash(), notNullValue());
  }

  @Test
  void propertyToEntity_shouldReturnCorrectEntity_ifGivenPropertyIsEmptyAndTypeNotNull() {
    // given
    var property = new Property2();
    var type = "type";
    var expectedType = new ResourceType().setSimpleLabel(type);
    doReturn(expectedType).when(resourceTypeService).get(type);

    // when
    var resource = coreMapper.propertyToEntity(property, type);

    // then
    assertThat(resource.getLabel(), is(type));
    assertThat(resource.getType(), is(expectedType));
    assertThat(resource.getDoc(), is(propertyToDoc(property)));
    assertThat(resource.getResourceHash(), notNullValue());
  }

  @Test
  void provisionActivityToEntity_shouldThrowNpe_ifGivenProvisionActivityIsNull() {
    // given
    ProvisionActivity2 dto = null;
    var label = "label";
    var type = "type";

    // when
    NullPointerException thrown = assertThrows(NullPointerException.class,
      () -> coreMapper.provisionActivityToEntity(dto, label, type));

    // then
    assertThat(thrown.getMessage(), is("dto is marked non-null but is null"));
  }

  @Test
  void provisionActivityToEntity_shouldThrowNpe_ifGivenResourceTypeIsNull() {
    // given
    var dto = new ProvisionActivity2();
    var label = "label";
    String type = null;

    // when
    NullPointerException thrown = assertThrows(NullPointerException.class,
      () -> coreMapper.provisionActivityToEntity(dto, label, type));

    // then
    assertThat(thrown.getMessage(), is("resourceType is marked non-null but is null"));
  }

  @Test
  void provisionActivityToEntity_shouldReturnCorrectEntity_ifGivenDtoAndTypeNotNull() {
    // given
    var place1 = new Property2().id("id1").label("label1").uri("uri1");
    var type = "type";
    var expectedType = new ResourceType().setSimpleLabel(type);
    doReturn(expectedType).when(resourceTypeService).get(type);
    var expectedPlaceType = new ResourceType().setSimpleLabel(PLACE_COMPONENTS);
    doReturn(expectedPlaceType).when(resourceTypeService).get(PLACE_COMPONENTS);
    var expectedPlacePredicate = new Predicate(PLACE2_PRED);
    doReturn(expectedPlacePredicate).when(predicateService).get(PLACE2_PRED);
    var expectedTarget1 = new Resource()
      .setType(expectedPlaceType)
      .setLabel(place1.getLabel())
      .setDoc(propertyToDoc(place1));
    expectedTarget1
      .setResourceHash(coreMapper.hash(expectedTarget1));
    var place2 = new Property2().id("id2").label("label2").uri("uri2");
    var expectedTarget2 = new Resource()
      .setType(expectedPlaceType)
      .setLabel(place2.getLabel())
      .setDoc(propertyToDoc(place2));
    expectedTarget2
      .setResourceHash(coreMapper.hash(expectedTarget2));
    var dto = new ProvisionActivity2()
      .date(List.of("date1", "date2"))
      .simpleAgent(List.of("agent1", "agent2"))
      .simpleDate(List.of("s-date1", "s-date2"))
      .simplePlace(List.of("place1", "place2"))
      .place(List.of(place1, place2));
    var label = "label";

    // when
    var resource = coreMapper.provisionActivityToEntity(dto, label, type);

    // then
    assertThat(resource.getLabel(), is(label));
    assertThat(resource.getType(), is(expectedType));
    assertThat(resource.getDoc(), is(provisionActivityToDoc(dto)));
    assertThat(resource.getResourceHash(), notNullValue());
    assertThat(resource.getOutgoingEdges(), hasSize(2));
    var edgesAreExpected = resource.getOutgoingEdges().stream().allMatch(edge ->
      edge.getPredicate().equals(expectedPlacePredicate)
        && edge.getSource().equals(resource)
        && (edge.getTarget().equals(expectedTarget1)) || edge.getTarget().equals(expectedTarget2));
    assertThat(edgesAreExpected, is(true));
  }

  @Test
  void provisionActivityToEntity_shouldReturnCorrectEntity_ifGivenDtoAndTypeNotNullButLabelIsNull() {
    // given
    var place1 = new Property2().id("id1").uri("uri1");
    var type = "type";
    var expectedType = new ResourceType().setSimpleLabel(type);
    doReturn(expectedType).when(resourceTypeService).get(type);
    var expectedPlaceType = new ResourceType().setSimpleLabel(PLACE_COMPONENTS);
    doReturn(expectedPlaceType).when(resourceTypeService).get(PLACE_COMPONENTS);
    var expectedPlacePredicate = new Predicate(PLACE2_PRED);
    doReturn(expectedPlacePredicate).when(predicateService).get(PLACE2_PRED);
    var expectedTarget1 = new Resource()
      .setType(expectedPlaceType)
      .setLabel(PLACE_COMPONENTS)
      .setDoc(propertyToDoc(place1));
    expectedTarget1
      .setResourceHash(coreMapper.hash(expectedTarget1));
    var place2 = new Property2().id("id2").uri("uri2");
    var expectedTarget2 = new Resource()
      .setType(expectedPlaceType)
      .setLabel(PLACE_COMPONENTS)
      .setDoc(propertyToDoc(place2));
    expectedTarget2
      .setResourceHash(coreMapper.hash(expectedTarget2));
    var dto = new ProvisionActivity2()
      .date(List.of("date1", "date2"))
      .simpleAgent(List.of("agent1", "agent2"))
      .simpleDate(List.of("s-date1", "s-date2"))
      .simplePlace(List.of("place1", "place2"))
      .place(List.of(place1, place2));
    String label = null;

    // when
    var resource = coreMapper.provisionActivityToEntity(dto, label, type);

    // then
    assertThat(resource.getLabel(), is(type));
    assertThat(resource.getType(), is(expectedType));
    assertThat(resource.getDoc(), is(provisionActivityToDoc(dto)));
    assertThat(resource.getResourceHash(), notNullValue());
    assertThat(resource.getOutgoingEdges(), hasSize(2));
    var edgesAreExpected = resource.getOutgoingEdges().stream().allMatch(edge ->
      edge.getPredicate().equals(expectedPlacePredicate)
        && edge.getSource().equals(resource)
        && (edge.getTarget().equals(expectedTarget1)) || edge.getTarget().equals(expectedTarget2));
    assertThat(edgesAreExpected, is(true));
  }
}
