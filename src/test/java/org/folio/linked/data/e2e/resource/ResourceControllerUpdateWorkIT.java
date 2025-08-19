package org.folio.linked.data.e2e.resource;

import static org.folio.linked.data.e2e.resource.ResourceControllerITBase.RESOURCE_URL;
import static org.folio.linked.data.test.TestUtil.awaitAndAssert;
import static org.folio.linked.data.test.TestUtil.defaultHeaders;
import static org.folio.linked.data.test.TestUtil.readTree;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.folio.ld.dictionary.PredicateDictionary;
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
    var work = getWork();
    var instance = getInstance(work);
    resourceTestService.saveGraph(instance);

    // when
    var workUpdateRequestDto = getWorkRequestDto(person.getId(), instance.getId());

    var updateRequest = put(RESOURCE_URL + "/" + work.getId())
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env))
      .content(workUpdateRequestDto);

    mockMvc.perform(updateRequest).andExpect(status().isOk());

    // then
    awaitAndAssert(() ->
      assertTrue(inventoryTopicListener.getMessages().stream()
        .anyMatch(m -> isExpectedEvent(m, instance.getId()))
      )
    );
  }

  private String getWorkRequestDto(Long personId, Long instanceId) {
    return """
      {
        "resource": {
          "http://bibfra.me/vocab/lite/Work": {
            "profileId": 2,
            "http://bibfra.me/vocab/marc/title": [
                {
                  "http://bibfra.me/vocab/marc/Title": {
                      "http://bibfra.me/vocab/marc/mainTitle": [ "simple_work" ]
                  }
                }
            ],
            "_creatorReference": [ { "id": "%PERSON_ID%" } ],
            "_instanceReference": [ { "id": "%INSTANCE_ID%"} ]
          }
        }
      }
      """
      .replace("%PERSON_ID%", personId.toString())
      .replace("%INSTANCE_ID%", instanceId.toString());
  }

  private Resource getWork() {
    var title = new Resource()
      .addTypes(ResourceTypeDictionary.TITLE)
      .setDoc(readTree("""
        {
          "http://bibfra.me/vocab/marc/mainTitle": ["simple_work"]
        }
        """))
      .setLabel("simple_work");
    var work = new Resource()
      .addTypes(ResourceTypeDictionary.WORK)
      .setDoc(readTree("{}"))
      .setLabel("simple_work");

    work.addOutgoingEdge(new ResourceEdge(work, title, PredicateDictionary.TITLE));

    title.setId(hashService.hash(title));
    work.setId(hashService.hash(work));

    return work;
  }

  private Resource getInstance(Resource work) {
    var title = new Resource()
      .addTypes(ResourceTypeDictionary.TITLE)
      .setDoc(readTree("""
        {
          "http://bibfra.me/vocab/marc/mainTitle": ["simple_instance"]
        }
        """))
      .setLabel("simple_instance");
    var instance = new Resource()
      .addTypes(ResourceTypeDictionary.INSTANCE)
      .setDoc(readTree("{}"))
      .setLabel("simple_instance");

    instance.addOutgoingEdge(new ResourceEdge(instance, title, PredicateDictionary.TITLE));
    instance.addOutgoingEdge(new ResourceEdge(instance, work, PredicateDictionary.INSTANTIATES));

    title.setId(hashService.hash(title));
    instance.setId(hashService.hash(instance));

    return instance;
  }

  private Resource getPerson() {
    var person = new Resource()
      .addTypes(ResourceTypeDictionary.PERSON)
      .setDoc(readTree("""
        {
          "http://bibfra.me/vocab/lite/name": ["Person name"]
        }
        """))
      .setLabel("Person name");

    var lccn = new Resource()
      .addTypes(ResourceTypeDictionary.ID_LCCN, ResourceTypeDictionary.IDENTIFIER)
      .setDoc(readTree("""
        {
          "http://bibfra.me/vocab/lite/link": ["n123456789"]
        }
        """))
      .setLabel("n123456789");

    person.setFolioMetadata(new FolioMetadata(person).setInventoryId("123456789"));
    person.addOutgoingEdge(new ResourceEdge(person, lccn, PredicateDictionary.MAP));

    person.setId(hashService.hash(person));
    lccn.setId(hashService.hash(lccn));

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
}
