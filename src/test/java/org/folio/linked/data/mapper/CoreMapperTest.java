package org.folio.linked.data.mapper;

import static org.folio.linked.data.test.IsEqualJson.equalToJson;
import static org.folio.linked.data.test.TestUtil.OBJECT_MAPPER;
import static org.folio.linked.data.test.TestUtil.getJsonNode;
import static org.folio.linked.data.test.TestUtil.getPropertyNode;
import static org.folio.linked.data.test.TestUtil.getResourceSample;
import static org.folio.linked.data.test.TestUtil.getSameAsJsonNode;
import static org.folio.linked.data.test.TestUtil.propertyToDoc;
import static org.folio.linked.data.test.TestUtil.provisionActivityToDoc;
import static org.folio.linked.data.test.TestUtil.random;
import static org.folio.linked.data.util.BibframeConstants.AGENT_PRED;
import static org.folio.linked.data.util.BibframeConstants.DATE_PRED;
import static org.folio.linked.data.util.BibframeConstants.PLACE_COMPONENTS;
import static org.folio.linked.data.util.BibframeConstants.PLACE_PRED;
import static org.folio.linked.data.util.BibframeConstants.ROLE_PRED;
import static org.folio.linked.data.util.BibframeConstants.SAME_AS_PRED;
import static org.folio.linked.data.util.BibframeConstants.SIMPLE_AGENT_PRED;
import static org.folio.linked.data.util.BibframeConstants.SIMPLE_DATE_PRED;
import static org.folio.linked.data.util.BibframeConstants.SIMPLE_PLACE_PRED;
import static org.folio.linked.data.util.BibframeConstants.VALUE_URL;
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
import com.fasterxml.jackson.databind.node.TextNode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import org.folio.linked.data.domain.dto.BibframeResponse;
import org.folio.linked.data.domain.dto.Contribution;
import org.folio.linked.data.domain.dto.ImmediateAcquisition;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.domain.dto.PersonField;
import org.folio.linked.data.domain.dto.Property;
import org.folio.linked.data.domain.dto.ProvisionActivity;
import org.folio.linked.data.domain.dto.Url;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.CoreMapperImpl;
import org.folio.linked.data.mapper.resource.common.inner.InnerResourceMapper;
import org.folio.linked.data.mapper.resource.common.inner.sub.SubResourceMapper;
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
    assertThat(property, is(new Property()));
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
    assertThat(provisionActivity, is(new ProvisionActivity()));
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
    assertThat(url, is(new Url()));
  }

  @Test
  void toUrl_shouldReturnCorrectProperty_ifGivenResourceContainsCorrectDoc() {
    // given
    var map = Map.of(
      VALUE_URL, List.of("value", "value2")
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
  void addMappedResources_shouldThrowNpe_ifGivenSubResourceMapperIsNull(@Mock Resource resource,
                                                                        @Mock Consumer<Instance> consumer) {
    // given
    SubResourceMapper subResourceMapper = null;

    // when
    NullPointerException thrown = assertThrows(NullPointerException.class,
      () -> coreMapper.addMappedResources(subResourceMapper, resource, consumer, Instance.class));

    // then
    assertThat(thrown.getMessage(), is("subResourceMapper is marked non-null but is null"));
  }

  @Test
  void addMappedResources_shouldThrowNpe_ifGivenResourceIsNull(@Mock SubResourceMapper subResourceMapper,
                                                               @Mock Consumer<Instance> consumer) {
    // given
    Resource resource = null;

    // when
    NullPointerException thrown = assertThrows(NullPointerException.class,
      () -> coreMapper.addMappedResources(subResourceMapper, resource, consumer, Instance.class));

    // then
    assertThat(thrown.getMessage(), is("resource is marked non-null but is null"));
  }

  @Test
  void addMappedResources_shouldThrowNpe_ifGivenConsumerIsNull(@Mock SubResourceMapper subResourceMapper,
                                                               @Mock Resource resource) {
    // given
    Consumer<Instance> consumer = null;

    // when
    NullPointerException thrown = assertThrows(NullPointerException.class,
      () -> coreMapper.addMappedResources(subResourceMapper, resource, consumer, Instance.class));

    // then
    assertThat(thrown.getMessage(), is("consumer is marked non-null but is null"));
  }

  @Test
  void addMappedResources_shouldThrowNpe_ifGivenDestinationIsNull(@Mock SubResourceMapper subResourceMapper,
                                                                  @Mock Consumer<Instance> consumer,
                                                                  @Mock Resource resource) {
    // given
    Class destination = null;

    // when
    NullPointerException thrown = assertThrows(NullPointerException.class,
      () -> coreMapper.addMappedResources(subResourceMapper, resource, consumer, destination));

    // then
    assertThat(thrown.getMessage(), is("destination is marked non-null but is null"));
  }

  @Test
  void addMappedResources_shouldAddDestinationObjectToGivenConsumer_ifGivenResourceIsEmpty(
    @Mock SubResourceMapper subResourceMapper) {
    // given
    var bibframeResponse = new BibframeResponse();
    var resource = new Resource();

    // when
    coreMapper.addMappedResources(subResourceMapper, resource, bibframeResponse::addInstanceItem, Instance.class);

    // then
    assertThat(bibframeResponse.getInstance(), hasSize(1));
  }

  @Test
  void addMappedResources_shouldAddDestinationObjectToGivenConsumer_ifGivenResourceContainsDoc(
    @Mock SubResourceMapper subResourceMapper) {
    // given
    var bibframeResponse = new BibframeResponse();
    var node = OBJECT_MAPPER.createObjectNode();
    var resource = new Resource().setDoc(node);

    // when
    coreMapper.addMappedResources(subResourceMapper, resource, bibframeResponse::addInstanceItem, Instance.class);

    // then
    assertThat(bibframeResponse.getInstance(), hasSize(1));
  }

  @Test
  void addMappedResources_shouldAddDestinationObjectToGivenConsumerAndMapEdge_ifGivenResourceContainsDocAndEdge(
    @Mock SubResourceMapper subResourceMapper) {
    // given
    var bibframeResponse = new BibframeResponse();
    var node = OBJECT_MAPPER.createObjectNode();
    var resource = new Resource().setDoc(node);
    var target = new Resource().setLabel("target");
    var edge = new ResourceEdge(resource, target, new Predicate("pred"));
    resource.setOutgoingEdges(Set.of(edge));

    // when
    coreMapper.addMappedResources(subResourceMapper, resource, bibframeResponse::addInstanceItem, Instance.class);

    // then
    assertThat(bibframeResponse.getInstance(), hasSize(1));
    verify(subResourceMapper).toDto(eq(edge), any(Instance.class));
  }

  @Test
  void addMappedProperties_shouldThrowNpe_ifGivenResourceIsNull(@Mock Consumer<Property> consumer) {
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
  void addMappedProperties_shouldThrowNpe_ifGivenPredicateIsNull(@Mock Consumer<Property> consumer) {
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
    Consumer<Property> consumer = null;

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
    var immediateAcquisition = new ImmediateAcquisition();

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
    var immediateAcquisition = new ImmediateAcquisition();

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

    var immediateAcquisition = new ImmediateAcquisition();

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

    var immediateAcquisition = new ImmediateAcquisition();

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
    var dtoClass = Property.class;

    // when
    var result = coreMapper.readResourceDoc(resource, dtoClass);

    // then
    assertThat(result, is(new Property()));
  }

  @Test
  void readResourceDoc_shouldReturnCorrectDto_ifGivenResourceHasDoc() {
    // given
    var node = getPropertyNode("id", "label", "uri");
    var resource = new Resource().setDoc(node);
    var dtoClass = Property.class;

    // when
    var result = coreMapper.readResourceDoc(resource, dtoClass);

    // then
    assertThat(result.getId(), is("id"));
    assertThat(result.getLabel(), is("label"));
    assertThat(result.getUri(), is("uri"));
  }

  @Test
  void addMappedPersonLookups_shouldThrowNpe_ifGivenResourceIsNull(@Mock Consumer<PersonField> personConsumer) {
    // given
    Resource resource = null;
    var predicate = "predicate";

    // when
    NullPointerException thrown = assertThrows(NullPointerException.class,
      () -> coreMapper.addMappedPersonLookups(resource, predicate, personConsumer));

    // then
    assertThat(thrown.getMessage(), is("resource is marked non-null but is null"));
  }

  @Test
  void addMappedPersonLookups_shouldThrowNpe_ifGivenPredicateIsNull(@Mock Consumer<PersonField> personConsumer) {
    // given
    var resource = new Resource();
    String predicate = null;

    // when
    NullPointerException thrown = assertThrows(NullPointerException.class,
      () -> coreMapper.addMappedPersonLookups(resource, predicate, personConsumer));

    // then
    assertThat(thrown.getMessage(), is("predicate is marked non-null but is null"));
  }

  @Test
  void addMappedPersonLookups_shouldThrowNpe_ifGivenConsumerIsNull() {
    // given
    var resource = new Resource();
    var predicate = "predicate";
    Consumer<PersonField> personConsumer = null;

    // when
    NullPointerException thrown = assertThrows(NullPointerException.class,
      () -> coreMapper.addMappedPersonLookups(resource, predicate, personConsumer));

    // then
    assertThat(thrown.getMessage(), is("personConsumer is marked non-null but is null"));
  }

  @Test
  void addMappedPersonLookups_shouldAddNothing_ifTargetContainsNoEdges() {
    // given
    var source = new Resource();
    var contribution = new Contribution();

    // when
    coreMapper.addMappedPersonLookups(source, AGENT_PRED, contribution::addAgentItem);

    // then
    assertThat(contribution.getAgent(), nullValue());
  }

  @Test
  void addMappedPersonLookups_shouldAddNothing_ifTargetContainsEdgeWithDifferentPredicate() {
    // given
    var source = new Resource();
    var target = new Resource();
    source.getOutgoingEdges().add(new ResourceEdge(source, target, new Predicate(ROLE_PRED)));
    var contribution = new Contribution();

    // when
    coreMapper.addMappedPersonLookups(source, AGENT_PRED, contribution::addAgentItem);

    // then
    assertThat(contribution.getAgent(), nullValue());
  }

  @Test
  void addMappedPersonLookups_shouldAddNothing_ifTargetContainsEdgeWithNoDoc() {
    // given
    var source = new Resource();
    var target = new Resource();
    source.getOutgoingEdges().add(new ResourceEdge(source, target, new Predicate(AGENT_PRED)));
    var contribution = new Contribution();

    // when
    coreMapper.addMappedPersonLookups(source, AGENT_PRED, contribution::addAgentItem);

    // then
    assertThat(contribution.getAgent(), nullValue());
  }

  @Test
  void addMappedPersonLookups_shouldAddNothing_ifTargetContainsNotSameAsDocInEdge() {
    // given
    var source = new Resource();
    var target = new Resource();
    target.setDoc(new TextNode("abc"));
    source.getOutgoingEdges().add(new ResourceEdge(source, target, new Predicate(AGENT_PRED)));
    var contribution = new Contribution();

    // when
    coreMapper.addMappedPersonLookups(source, AGENT_PRED, contribution::addAgentItem);

    // then
    assertThat(contribution.getAgent(), nullValue());
  }

  @Test
  void addMappedPersonLookups_shouldAddNothing_ifTargetContainsEmptySameAsDocInEdge() {
    // given
    var source = new Resource();
    var target = new Resource();
    var arrayNode = OBJECT_MAPPER.createArrayNode();
    var node = OBJECT_MAPPER.createObjectNode();
    node.set(SAME_AS_PRED, arrayNode);
    target.setDoc(node);
    source.getOutgoingEdges().add(new ResourceEdge(source, target, new Predicate(AGENT_PRED)));
    var contribution = new Contribution();

    // when
    coreMapper.addMappedPersonLookups(source, AGENT_PRED, contribution::addAgentItem);

    // then
    assertThat(contribution.getAgent(), nullValue());
  }

  @Test
  void addMappedPersonLookups_shouldAddPerson_ifTargetContainsCorrectSameAsDocInEdge() {
    // given
    var target = new Resource();
    var label = "label";
    var uri = "uri";
    target.setDoc(getSameAsJsonNode(getPropertyNode(null, label, uri)));
    var source = new Resource();
    source.getOutgoingEdges().add(new ResourceEdge(source, target, new Predicate(AGENT_PRED)));
    var contribution = new Contribution();

    // when
    coreMapper.addMappedPersonLookups(source, AGENT_PRED, contribution::addAgentItem);

    // then
    assertThat(contribution.getAgent(), hasSize(1));
    assertThat(contribution.getAgent().get(0).getPerson().getSameAs(), hasSize(1));
    assertThat(contribution.getAgent().get(0).getPerson().getSameAs().get(0).getLabel(), is(label));
    assertThat(contribution.getAgent().get(0).getPerson().getSameAs().get(0).getUri(), is(uri));
  }

  @Test
  void addMappedPersonLookups_shouldAddPersons_ifTargetContainsCorrectManeSameAsDocInEdge() {
    // given
    var target = new Resource();
    var label = "label";
    var uri = "uri";
    var label2 = "label2";
    var uri2 = "uri2";
    target.setDoc(getSameAsJsonNode(getPropertyNode(null, label, uri), getPropertyNode(null, label2, uri2)));
    var source = new Resource();
    source.getOutgoingEdges().add(new ResourceEdge(source, target, new Predicate(AGENT_PRED)));
    var contribution = new Contribution();

    // when
    coreMapper.addMappedPersonLookups(source, AGENT_PRED, contribution::addAgentItem);

    // then
    assertThat(contribution.getAgent(), hasSize(1));
    assertThat(contribution.getAgent().get(0).getPerson().getSameAs(), hasSize(2));
    assertThat(contribution.getAgent().get(0).getPerson().getSameAs().get(0).getLabel(), is(label));
    assertThat(contribution.getAgent().get(0).getPerson().getSameAs().get(0).getUri(), is(uri));
    assertThat(contribution.getAgent().get(0).getPerson().getSameAs().get(1).getLabel(), is(label2));
    assertThat(contribution.getAgent().get(0).getPerson().getSameAs().get(1).getUri(), is(uri2));
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
  void hash_shouldReturnHashUtilResultForResourceDoc_ifGivenResourceContainsDoc() {
    // given
    var node = getPropertyNode("id", "label", "uri");
    var resource = new Resource().setDoc(node);

    // when
    var result = coreMapper.hash(resource);

    // then
    assertThat(result, is(HashUtil.hash(resource.getDoc())));
  }

  @Test
  void hash_shouldReturnHashUtilResultForEmptyNode_ifGivenResourceContainsNoDocAndEdges() {
    // given
    var resource = new Resource();

    // when
    var result = coreMapper.hash(resource);

    // then
    assertThat(result, is(HashUtil.hash(OBJECT_MAPPER.createObjectNode())));
  }

  @Test
  void hash_shouldReturnHashUtilResultForNodeOfEdgeJsons_ifGivenResourceContainsNoDocButEdges() {
    // given
    var resource = new Resource();
    var targetNode1 = getPropertyNode("id", "label", "uri");
    var target1 = new Resource().setDoc(targetNode1).setResourceHash(111L);
    var predicate1 = new Predicate("predicate1");
    var targetNode2 = getPropertyNode("id2", "label2", "uri2");
    var target2 = new Resource().setDoc(targetNode2).setResourceHash(222L);
    var predicate2 = new Predicate("predicate2");
    var targetNode3 = getPropertyNode("id3", "label3", "uri3");
    var target3 = new Resource().setDoc(targetNode3).setResourceHash(333L);
    resource.getOutgoingEdges()
      .add(new ResourceEdge(resource, target1, predicate1));
    resource.getOutgoingEdges()
      .add(new ResourceEdge(resource, target2, predicate1));
    resource.getOutgoingEdges()
      .add(new ResourceEdge(resource, target3, predicate2));

    var expectedNodeForHash = OBJECT_MAPPER.createObjectNode();
    var arrayPredicate1 = OBJECT_MAPPER.createArrayNode();
    arrayPredicate1.add(targetNode1);
    arrayPredicate1.add(targetNode2);
    expectedNodeForHash.set(predicate1.getLabel(), arrayPredicate1);
    var arrayPredicate2 = OBJECT_MAPPER.createArrayNode();
    arrayPredicate2.add(targetNode3);
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
    var resource = new Resource().setDoc(rootNode);
    var targetNode1 = getPropertyNode("id", "label", "uri");
    var target1 = new Resource().setDoc(targetNode1).setResourceHash(111L);
    var predicate1 = new Predicate("predicate1");
    var targetNode2 = getPropertyNode("id2", "label2", "uri2");
    var target2 = new Resource().setDoc(targetNode2).setResourceHash(222L);
    var predicate2 = new Predicate("predicate2");
    var targetNode3 = getPropertyNode("id3", "label3", "uri3");
    var target3 = new Resource().setDoc(targetNode3).setResourceHash(333L);
    resource.getOutgoingEdges()
      .add(new ResourceEdge(resource, target1, predicate1));
    resource.getOutgoingEdges()
      .add(new ResourceEdge(resource, target2, predicate1));
    resource.getOutgoingEdges()
      .add(new ResourceEdge(resource, target3, predicate2));

    var expectedNodeForHash = rootNode.deepCopy();
    var arrayPredicate1 = OBJECT_MAPPER.createArrayNode();
    arrayPredicate1.add(targetNode1);
    arrayPredicate1.add(targetNode2);
    expectedNodeForHash.set(predicate1.getLabel(), arrayPredicate1);
    var arrayPredicate2 = OBJECT_MAPPER.createArrayNode();
    arrayPredicate2.add(targetNode3);
    expectedNodeForHash.set(predicate2.getLabel(), arrayPredicate2);

    // when
    var result = coreMapper.hash(resource);

    // then
    assertThat(result, is(HashUtil.hash(expectedNodeForHash)));
  }

  @Test
  void toJson_shouldReturnCorrectJsonNodeFromString() throws JsonProcessingException {
    // given
    var json = getResourceSample();

    // when
    var jsonNode = coreMapper.toJson(json);

    // then
    assertThat(OBJECT_MAPPER.writeValueAsString(jsonNode), equalToJson(getResourceSample()));
  }

  @Test
  void toJson_shouldReturnCorrectJsonNodeFromMap() throws JsonProcessingException {
    // given
    var json = getResourceSample();
    var map = OBJECT_MAPPER.readValue(json, new TypeReference<Map<String, Object>>() {
    });

    // when
    var jsonNode = coreMapper.toJson(map);

    // then
    assertThat(OBJECT_MAPPER.writeValueAsString(jsonNode), equalToJson(getResourceSample()));
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
    var dto1 = new Property().id("id").label("label").uri("uri");
    var dto2 = new Property().id("id2").label("label2").uri("uri2");
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
    var dto1 = new Property().id("id").label("label").uri("uri");
    var dto2 = new Property().id("id2").label("label2").uri("uri2");
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
    var dto1 = new Property().id("id").label("label").uri("uri");
    var dto2 = new Property().id("id2").label("label2").uri("uri2");
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

  @Test
  void mapPropertyEdges_shouldThrowNpe_ifGivenSourceIsNull() {
    // given
    var subProperties = new ArrayList<Property>();
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
    var subProperties = new ArrayList<Property>();
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
    var subProperties = new ArrayList<Property>();
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
    List<Property> subProperties = null;
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
    var subProperties = new ArrayList<Property>();
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
    var dto1 = new Property().id("id").label("label").uri("uri");
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
    var dto2 = new Property().id("id2").label("label2").uri("uri2");
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
    Property property = null;
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
    var property = new Property();
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
    var property = random(Property.class);
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
  void provisionActivityToEntity_shouldThrowNpe_ifGivenProvisionActivityIsNull() {
    // given
    ProvisionActivity dto = null;
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
    var dto = new ProvisionActivity();
    var label = "label";
    String type = null;

    // when
    NullPointerException thrown = assertThrows(NullPointerException.class,
      () -> coreMapper.provisionActivityToEntity(dto, label, type));

    // then
    assertThat(thrown.getMessage(), is("resourceType is marked non-null but is null"));
  }

  @Test
  void provisionActivityToEntity_shouldReturnCorrectEntity_ifGivenPropertyAndTypeNotNull() {
    // given
    var place1 = new Property().id("id1").label("label1").uri("uri1");
    var type = "type";
    var expectedType = new ResourceType().setSimpleLabel(type);
    doReturn(expectedType).when(resourceTypeService).get(type);
    var expectedPlaceType = new ResourceType().setSimpleLabel(PLACE_COMPONENTS);
    doReturn(expectedPlaceType).when(resourceTypeService).get(PLACE_COMPONENTS);
    var expectedPlacePredicate = new Predicate(PLACE_PRED);
    doReturn(expectedPlacePredicate).when(predicateService).get(PLACE_PRED);
    var expectedTarget1 = new Resource()
      .setType(expectedPlaceType)
      .setLabel(place1.getLabel())
      .setDoc(propertyToDoc(place1));
    expectedTarget1
      .setResourceHash(coreMapper.hash(expectedTarget1));
    var place2 = new Property().id("id2").label("label2").uri("uri2");
    var expectedTarget2 = new Resource()
      .setType(expectedPlaceType)
      .setLabel(place2.getLabel())
      .setDoc(propertyToDoc(place2));
    expectedTarget2
      .setResourceHash(coreMapper.hash(expectedTarget2));
    var dto = new ProvisionActivity()
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
}
