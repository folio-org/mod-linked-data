package org.folio.linked.data.mapper;

import static org.folio.linked.data.test.IsEqualJson.equalToJson;
import static org.folio.linked.data.test.TestUtil.getResourceSample;
import static org.folio.linked.data.util.BibframeConstants.AGENT_PRED;
import static org.folio.linked.data.util.BibframeConstants.PROPERTY_LABEL;
import static org.folio.linked.data.util.BibframeConstants.PROPERTY_URI;
import static org.folio.linked.data.util.BibframeConstants.ROLE_PRED;
import static org.folio.linked.data.util.BibframeConstants.SAME_AS_PRED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import java.util.Arrays;
import java.util.Map;
import org.folio.linked.data.configuration.ObjectMapperConfig;
import org.folio.linked.data.domain.dto.Contribution;
import org.folio.linked.data.mapper.resource.common.CommonMapperImpl;
import org.folio.linked.data.model.entity.Predicate;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.model.entity.ResourceType;
import org.folio.linked.data.service.dictionary.DictionaryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CommonMapperTest {

  @InjectMocks
  private CommonMapperImpl commonMapper;
  @Mock
  private DictionaryService<ResourceType> resourceTypeService;
  @Mock
  private DictionaryService<Predicate> predicateService;
  @Spy
  private ObjectMapper objectMapper = new ObjectMapperConfig().objectMapper();

  @Test
  void toJson_shouldReturnCorrectJsonNodeFromString() throws JsonProcessingException {
    // given
    var json = getResourceSample();

    // when
    var jsonNode = commonMapper.toJson(json);

    // then
    assertThat(objectMapper.writeValueAsString(jsonNode), equalToJson(getResourceSample()));
  }

  @Test
  void toJson_shouldReturnCorrectJsonNodeFromMap() throws JsonProcessingException {
    // given
    var json = getResourceSample();
    var map = objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {
    });

    // when
    var jsonNode = commonMapper.toJson(map);

    // then
    assertThat(objectMapper.writeValueAsString(jsonNode), equalToJson(getResourceSample()));
  }


  @Test
  void toJson_shouldReturnEmptyJsonNodeForNullInput() throws JsonProcessingException {
    // given
    Object configuration = null;

    // when
    var jsonNode = commonMapper.toJson(configuration);

    // then
    assertThat(objectMapper.writeValueAsString(jsonNode), equalToJson("{}"));
  }

  @Test
  void addMappedPersonLookups_shouldAddNothing_ifTargetContainsNoEdges() {
    // given
    var source = new Resource();
    var contribution = new Contribution();

    // when
    commonMapper.addMappedPersonLookups(source, AGENT_PRED, contribution::addAgentItem);

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
    commonMapper.addMappedPersonLookups(source, AGENT_PRED, contribution::addAgentItem);

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
    commonMapper.addMappedPersonLookups(source, AGENT_PRED, contribution::addAgentItem);

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
    commonMapper.addMappedPersonLookups(source, AGENT_PRED, contribution::addAgentItem);

    // then
    assertThat(contribution.getAgent(), nullValue());
  }

  @Test
  void addMappedPersonLookups_shouldAddNothing_ifTargetContainsEmptySameAsDocInEdge() {
    // given
    var objectMapper = new ObjectMapper();
    var source = new Resource();
    var target = new Resource();
    var arrayNode = objectMapper.createArrayNode();
    var node = objectMapper.createObjectNode();
    node.set(SAME_AS_PRED, arrayNode);
    target.setDoc(node);
    source.getOutgoingEdges().add(new ResourceEdge(source, target, new Predicate(AGENT_PRED)));
    var contribution = new Contribution();

    // when
    commonMapper.addMappedPersonLookups(source, AGENT_PRED, contribution::addAgentItem);

    // then
    assertThat(contribution.getAgent(), nullValue());
  }

  @Test
  void addMappedPersonLookups_shouldAddPerson_ifTargetContainsCorrectSameAsDocInEdge() {
    // given
    var target = new Resource();
    var label = "label";
    var uri = "uri";
    target.setDoc(getSameAsJsonNode(getPropertyNode(label, uri)));
    var source = new Resource();
    source.getOutgoingEdges().add(new ResourceEdge(source, target, new Predicate(AGENT_PRED)));
    var contribution = new Contribution();

    // when
    commonMapper.addMappedPersonLookups(source, AGENT_PRED, contribution::addAgentItem);

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
    target.setDoc(getSameAsJsonNode(getPropertyNode(label, uri), getPropertyNode(label2, uri2)));
    var source = new Resource();
    source.getOutgoingEdges().add(new ResourceEdge(source, target, new Predicate(AGENT_PRED)));
    var contribution = new Contribution();

    // when
    commonMapper.addMappedPersonLookups(source, AGENT_PRED, contribution::addAgentItem);

    // then
    assertThat(contribution.getAgent(), hasSize(1));
    assertThat(contribution.getAgent().get(0).getPerson().getSameAs(), hasSize(2));
    assertThat(contribution.getAgent().get(0).getPerson().getSameAs().get(0).getLabel(), is(label));
    assertThat(contribution.getAgent().get(0).getPerson().getSameAs().get(0).getUri(), is(uri));
    assertThat(contribution.getAgent().get(0).getPerson().getSameAs().get(1).getLabel(), is(label2));
    assertThat(contribution.getAgent().get(0).getPerson().getSameAs().get(1).getUri(), is(uri2));
  }

  private JsonNode getSameAsJsonNode(ObjectNode... personNodes) {
    var arrayNode = objectMapper.createArrayNode();
    Arrays.stream(personNodes).forEach(arrayNode::add);
    var node = objectMapper.createObjectNode();
    node.set(SAME_AS_PRED, arrayNode);
    return node;
  }

  private ObjectNode getPropertyNode(String label, String uri) {
    var personNode = objectMapper.createObjectNode();
    personNode.put(PROPERTY_LABEL, label);
    personNode.put(PROPERTY_URI, uri);
    return personNode;
  }
}
