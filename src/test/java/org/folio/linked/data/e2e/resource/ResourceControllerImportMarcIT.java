package org.folio.linked.data.e2e.resource;

import static org.folio.linked.data.test.TestUtil.TEST_JSON_MAPPER;
import static org.folio.linked.data.test.TestUtil.awaitAndAssert;
import static org.folio.linked.data.test.TestUtil.defaultHeaders;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.folio.linked.data.e2e.ITBase;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.linked.data.integration.rest.srs.SrsClient;
import org.folio.linked.data.test.kafka.KafkaInventoryTopicListener;
import org.folio.linked.data.test.kafka.KafkaProducerTestConfiguration;
import org.folio.linked.data.test.kafka.KafkaSearchHubIndexTopicListener;
import org.folio.linked.data.test.kafka.KafkaSearchWorkIndexTopicListener;
import org.folio.rest.jaxrs.model.Record;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
@SpringBootTest(classes = {KafkaProducerTestConfiguration.class})
class ResourceControllerImportMarcIT extends ITBase {
  @MockitoBean
  private SrsClient srsClient;
  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private KafkaInventoryTopicListener inventoryTopicListener;
  @Autowired
  private KafkaSearchWorkIndexTopicListener searchWorkIndexTopicListener;
  @Autowired
  private KafkaSearchHubIndexTopicListener hubIndexTopicListener;

  @BeforeEach
  @Override
  public void beforeEach() {
    super.beforeEach();
    inventoryTopicListener.getMessages().clear();
    searchWorkIndexTopicListener.getMessages().clear();
    hubIndexTopicListener.getMessages().clear();
  }

  @Test
  void shouldImportMarc() throws Exception {
    var instanceId = UUID.randomUUID().toString();
    var sampleSrsResponse = """
      {
         "parsedRecord":{
            "content":{
               "fields":[
                  { "001":"in00000000001" },
                  { "008":"251031|                      ||| |      " },
                  {
                     "245":{
                        "ind1":" ",
                        "ind2":" ",
                        "subfields":[ { "a":"ResourceControllerImportMarcIT - Testing instance" } ]
                     }
                  },
                  {
                     "600":{
                        "ind1":" ",
                        "ind2":" ",
                        "subfields":[ { "a":"Person name" }, { "t":"Title" }, { "z":"Geographic subdivision" } ]
                     }
                  }
               ],
               "leader":"00258naa a2200085uu 4500"
            }
         }
      }
      """;


    when(srsClient.getSourceStorageInstanceRecordById(instanceId))
      .thenReturn(ResponseEntity.ok(TEST_JSON_MAPPER.readValue(sampleSrsResponse, Record.class)));

    var requestBuilder = post("/linked-data/inventory-instance/{inventoryId}/import", instanceId)
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env));

    var payload = mockMvc.perform(requestBuilder)
      .andExpect(status().isCreated())
      .andReturn()
      .getResponse()
      .getContentAsString();

    var resourceId = TEST_JSON_MAPPER.readTree(payload).get("id").asString();

    awaitAndAssert(
      () -> assertTrue(
        inventoryTopicListener.getMessages()
          .stream()
          .anyMatch(msg -> this.isExpectedInventoryMessage(msg, resourceId))
      )
    );

    awaitAndAssert(
      () -> assertTrue(
        searchWorkIndexTopicListener.getMessages()
          .stream()
          .anyMatch(this::isExpectedSearchWorkMessage)
      )
    );

    awaitAndAssert(
      () -> assertTrue(
        hubIndexTopicListener.getMessages()
          .stream()
          .anyMatch(this::isExpectedHubIndexMessage)
      )
    );
  }

  private boolean isExpectedInventoryMessage(String message, String resourceId) {
    var root = TEST_JSON_MAPPER.readTree(message);
    return root.get("eventType").asString().equals("UPDATE_INSTANCE")
      && root.get("eventPayload").get("sourceType").asString().equals("LINKED_DATA")
      && root.get("eventPayload").get("linkedDataId").asString().equals(resourceId)
      && message.contains("in00000000001");
  }

  private boolean isExpectedSearchWorkMessage(String message) {
    var root = TEST_JSON_MAPPER.readTree(message);
    return root.get("type").asString().equals("UPDATE")
      && root.get("resourceName").asString().equals("linked-data-work")
      && message.contains("ResourceControllerImportMarcIT - Testing instance");
  }

  private boolean isExpectedHubIndexMessage(String message) {
    var root = TEST_JSON_MAPPER.readTree(message);
    return root.get("type").asString().equals("CREATE")
      && root.get("resourceName").asString().equals("linked-data-hub")
      && root.get("new").get("label").asString().contains("Person name. Title");
  }
}
