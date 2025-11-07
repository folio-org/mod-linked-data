package org.folio.linked.data.e2e.resource;

import static org.folio.ld.dictionary.PredicateDictionary.EXPRESSION_OF;
import static org.folio.ld.dictionary.PredicateDictionary.INSTANTIATES;
import static org.folio.ld.dictionary.PredicateDictionary.MAP;
import static org.folio.ld.dictionary.PredicateDictionary.TITLE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.BOOKS;
import static org.folio.ld.dictionary.ResourceTypeDictionary.IDENTIFIER;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_LCCN;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PERSON;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.folio.linked.data.e2e.resource.ResourceControllerITBase.RESOURCE_URL;
import static org.folio.linked.data.test.TestUtil.awaitAndAssert;
import static org.folio.linked.data.test.TestUtil.defaultHeaders;
import static org.folio.linked.data.test.TestUtil.readTree;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.domain.dto.InstanceIngressEvent;
import org.folio.linked.data.e2e.ITBase;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.linked.data.model.entity.FolioMetadata;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.test.kafka.KafkaInventoryTopicListener;
import org.folio.linked.data.test.kafka.KafkaProducerTestConfiguration;
import org.folio.linked.data.test.resource.ResourceTestService;
import org.folio.marc4ld.service.marc2ld.reader.MarcReaderProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.marc4j.marc.DataField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@IntegrationTest
@SpringBootTest(classes = {KafkaProducerTestConfiguration.class})
class ResourceControllerUpdateWorkIT extends ITBase {
  @Autowired
  private ResourceTestService resourceTestService;
  @Autowired
  private ObjectMapper objectMapper;
  @Autowired
  private KafkaInventoryTopicListener inventoryTopicListener;
  @Autowired
  private MarcReaderProcessor marcReader;

  @BeforeEach
  @Override
  public void beforeEach() {
    super.beforeEach();
    inventoryTopicListener.getMessages().clear();
  }

  @Test
  void updateWork_should_send_update_instance_event_to_inventory() throws Exception {
    // given
    var person = getPerson();
    resourceTestService.saveGraph(person);
    var work = getWork("simple_work");
    var instance = getInstance(work);
    resourceTestService.saveGraph(instance);

    // when
    var workUpdateRequestDto = getWorkRequestDto("simple_work", person.getId(), instance.getId());
    putResource(work.getId(), workUpdateRequestDto);

    // then
    awaitAndAssert(() ->
      assertTrue(inventoryTopicListener.getMessages().stream()
        .anyMatch(m -> isExpectedEvent(m, instance.getId()))
      )
    );
  }

  @Test
  void updateWork_should_retain_incoming_edges() throws Exception {
    // given
    var work1 = getWork("work1");
    var work2 = getWork("work2");
    var edge = new ResourceEdge(work1, work2, EXPRESSION_OF);
    work1.addOutgoingEdge(edge);
    work2.addIncomingEdge(edge);
    resourceTestService.saveGraph(work2);
    var workUpdateRequestDto = getWorkRequestDto("updated work2", null, null);

    // when
    var putApiResponse = putResource(work2.getId(), workUpdateRequestDto);

    // then
    var updatedWork2Id = putApiResponse
      .path("resource")
      .path("http://bibfra.me/vocab/lite/Work")
      .path("id")
      .asLong();
    var work1Found = performGetGraph(work1.getId());
    var work2Found = performGetGraph(updatedWork2Id);
    assertTrue(work1Found.getOutgoingEdges().stream().anyMatch(e ->
      EXPRESSION_OF.getUri().equals(e.getPredicate().getUri()) && updatedWork2Id == e.getTarget().getId())
    );
    assertTrue(work2Found.getIncomingEdges().stream().anyMatch(e ->
      EXPRESSION_OF.getUri().equals(e.getPredicate().getUri()) && work1.getId().equals(e.getSource().getId()))
    );
  }

  private String getWorkRequestDto(String label, Long personId, Long instanceId) {
    var creatorRef = personId == null
      ? ""
      : ", \"_creatorReference\": [ { \"id\": \"" + personId + "\" } ]";
    var instanceRef = instanceId == null
      ? ""
      : ", \"_instanceReference\": [ { \"id\": \"" + instanceId + "\" } ]";
    return """
      {
        "resource": {
          "http://bibfra.me/vocab/lite/Work": {
            "profileId": 2,
            "http://bibfra.me/vocab/library/title": [
              {
                "http://bibfra.me/vocab/library/Title": {
                  "http://bibfra.me/vocab/library/mainTitle": [ "%s" ]
                }
              }
            ]%s%s
          }
        }
      }
      """.formatted(label, creatorRef, instanceRef);
  }

  private Resource getWork(String label) {
    var title = new Resource()
      .addTypes(ResourceTypeDictionary.TITLE)
      .setDoc(readTree("""
        {
          "http://bibfra.me/vocab/library/mainTitle": ["%s"]
        }
        """.formatted(label)))
      .setLabel(label);
    var work = new Resource()
      .addTypes(WORK, BOOKS)
      .setDoc(readTree("{}"))
      .setLabel(label);

    work.addOutgoingEdge(new ResourceEdge(work, title, TITLE));

    title.setIdAndRefreshEdges(hashService.hash(title));
    work.setIdAndRefreshEdges(hashService.hash(work));

    return work;
  }

  private Resource getInstance(Resource work) {
    var title = new Resource()
      .addTypes(ResourceTypeDictionary.TITLE)
      .setDoc(readTree("""
        {
          "http://bibfra.me/vocab/library/mainTitle": ["simple_instance"]
        }
        """))
      .setLabel("simple_instance");
    var instance = new Resource()
      .addTypes(INSTANCE)
      .setDoc(readTree("{}"))
      .setLabel("simple_instance");

    instance.addOutgoingEdge(new ResourceEdge(instance, title, TITLE));
    instance.addOutgoingEdge(new ResourceEdge(instance, work, INSTANTIATES));

    title.setIdAndRefreshEdges(hashService.hash(title));
    instance.setIdAndRefreshEdges(hashService.hash(instance));

    return instance;
  }

  private Resource getPerson() {
    var person = new Resource()
      .addTypes(PERSON)
      .setDoc(readTree("""
        {
          "http://bibfra.me/vocab/lite/name": ["Person name"]
        }
        """))
      .setLabel("Person name");

    var lccn = new Resource()
      .addTypes(ID_LCCN, IDENTIFIER)
      .setDoc(readTree("""
        {
          "http://bibfra.me/vocab/lite/link": ["n123456789"]
        }
        """))
      .setLabel("n123456789");

    person.setFolioMetadata(new FolioMetadata(person).setInventoryId("123456789"));
    person.addOutgoingEdge(new ResourceEdge(person, lccn, MAP));

    person.setIdAndRefreshEdges(hashService.hash(person));
    lccn.setIdAndRefreshEdges(hashService.hash(lccn));

    return person;
  }

  @SneakyThrows
  private <T> T parse(String json, Class<T> clazz) {
    return objectMapper.readValue(json, clazz);
  }

  private boolean isExpectedEvent(String eventStr, long linkedDataId) {
    var event = parse(eventStr, InstanceIngressEvent.class);
    var eventPayload = event.getEventPayload();
    var marc = eventPayload.getSourceRecordObject();
    return event.getEventType() == InstanceIngressEvent.EventTypeEnum.UPDATE_INSTANCE
      && eventPayload.getAdditionalProperties().get("linkedDataId").equals(linkedDataId)
      && isExpectedMarc(marc);
  }

  private boolean isExpectedMarc(String marcStr) {
    var marc = marcReader.readMarc(marcStr).toList().getFirst();
    var df100 = (DataField) marc.getVariableField("100");
    var df245 = (DataField) marc.getVariableField("245");
    var isDf100Valid = df100.getSubfields().stream().anyMatch(
      sf ->
        sf.getCode() == 'a' && sf.getData().equals("Person name")
          || sf.getCode() == '0' && sf.getData().equals("n123456789")
          || sf.getCode() == '9' && sf.getData().equals("123456789")
    );
    var isDf245Valid = df245.getSubfields().stream().anyMatch(
      sf -> sf.getCode() == 'a' && sf.getData().equals("simple_instance")
    );
    return isDf100Valid && isDf245Valid;
  }

  private org.folio.ld.dictionary.model.Resource performGetGraph(Long resourceId) throws Exception {
    var requestBuilder = get(RESOURCE_URL + "/" + resourceId + "/graph")
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env));
    var response = mockMvc.perform(requestBuilder)
      .andExpect(status().isOk())
      .andReturn().getResponse().getContentAsString();
    return objectMapper.readValue(response, org.folio.ld.dictionary.model.Resource.class);
  }

  private JsonNode putResource(Long resourceId, String dto) throws Exception {
    var updateRequest = put(RESOURCE_URL + "/" + resourceId)
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env))
      .content(dto);
    var response = mockMvc.perform(updateRequest)
      .andExpect(status().isOk())
      .andReturn().getResponse().getContentAsString();
    return objectMapper.readTree(response);
  }
}
