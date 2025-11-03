package org.folio.linked.data.e2e.resource;

import static org.folio.linked.data.test.TestUtil.OBJECT_MAPPER;
import static org.folio.linked.data.test.TestUtil.awaitAndAssert;
import static org.folio.linked.data.test.TestUtil.defaultHeaders;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import lombok.SneakyThrows;
import org.folio.linked.data.e2e.ITBase;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.linked.data.integration.client.SrsClient;
import org.folio.linked.data.test.kafka.KafkaInventoryTopicListener;
import org.folio.linked.data.test.kafka.KafkaProducerTestConfiguration;
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
  private final ObjectMapper mapper = OBJECT_MAPPER;

  @MockitoBean
  private SrsClient srsClient;
  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private KafkaInventoryTopicListener inventoryTopicListener;
  @Autowired
  private KafkaSearchWorkIndexTopicListener searchWorkIndexTopicListener;

  @BeforeEach
  @Override
  public void beforeEach() {
    super.beforeEach();
    inventoryTopicListener.getMessages().clear();
    searchWorkIndexTopicListener.getMessages().clear();
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
                  }
               ],
               "leader":"00258naa a2200085uu 4500"
            }
         }
      }
      """;


    when(srsClient.getSourceStorageInstanceRecordById(instanceId))
      .thenReturn(ResponseEntity.ok(mapper.readValue(sampleSrsResponse, Record.class)));

    var requestBuilder = post("/linked-data/inventory-instance/{inventoryId}/import", instanceId)
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env));

    var payload = mockMvc.perform(requestBuilder)
      .andExpect(status().isCreated())
      .andReturn()
      .getResponse()
      .getContentAsString();

    var resourceId = mapper.readTree(payload).get("id").asText();

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
  }

  @SneakyThrows
  private boolean isExpectedInventoryMessage(String message, String resourceId) {
    var root = mapper.readTree(message);
    return root.get("eventType").asText().equals("UPDATE_INSTANCE")
      && root.get("eventPayload").get("sourceType").asText().equals("LINKED_DATA")
      && root.get("eventPayload").get("linkedDataId").asText().equals(resourceId)
      && message.contains("in00000000001");
  }

  @SneakyThrows
  private boolean isExpectedSearchWorkMessage(String message) {
    var root = mapper.readTree(message);
    return root.get("type").asText().equals("UPDATE")
      && root.get("resourceName").asText().equals("linked-data-work")
      && message.contains("ResourceControllerImportMarcIT - Testing instance");
  }
}
