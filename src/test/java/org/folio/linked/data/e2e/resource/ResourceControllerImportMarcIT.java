package org.folio.linked.data.e2e.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.linked.data.model.entity.ResourceSource.LINKED_DATA;
import static org.folio.linked.data.test.TestUtil.TEST_JSON_MAPPER;
import static org.folio.linked.data.test.TestUtil.awaitAndAssert;
import static org.folio.linked.data.test.TestUtil.defaultHeaders;
import static org.folio.linked.data.test.TestUtil.getInstanceRequestDto;
import static org.folio.linked.data.test.resource.ResourceJsonPath.toCarrierCode;
import static org.folio.linked.data.test.resource.ResourceJsonPath.toCarrierLink;
import static org.folio.linked.data.test.resource.ResourceJsonPath.toCarrierTerm;
import static org.folio.linked.data.test.resource.ResourceJsonPath.toInstance;
import static org.folio.linked.data.test.resource.ResourceJsonPath.toMediaCode;
import static org.folio.linked.data.test.resource.ResourceJsonPath.toMediaLink;
import static org.folio.linked.data.test.resource.ResourceJsonPath.toMediaTerm;
import static org.folio.linked.data.test.resource.ResourceSpecUtil.createSpecRules;
import static org.folio.linked.data.test.resource.ResourceSpecUtil.createSpecifications;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.folio.linked.data.domain.dto.InstanceResponseField;
import org.folio.linked.data.domain.dto.ResourceResponseDto;
import org.folio.linked.data.e2e.base.ITBase;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.linked.data.integration.rest.specification.SpecClient;
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
  @MockitoBean
  private SpecClient specClient;
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

  @Test
  void shouldImportMarc_andStoreMediaCategory() throws Exception {
    var instanceId = UUID.randomUUID().toString();
    var sampleSrsResponse = """
      {
         "parsedRecord":{
            "content":{
               "fields":[
                  { "001":"in00000000002" },
                  {
                     "245":{
                        "ind1":" ",
                        "ind2":" ",
                        "subfields":[ { "a":"shouldImportMarc_andStoreMediaCategory - Testing media type" } ]
                     }
                  },
                  {
                     "337":{
                        "ind1":" ",
                        "ind2":" ",
                        "subfields":[ { "a":"computer" }, { "b":"s" } ]
                     }
                  }
               ],
               "leader":"00153nam a2200049uc 4500"
            }
         }
      }
      """;

    when(srsClient.getSourceStorageInstanceRecordById(instanceId))
      .thenReturn(ResponseEntity.ok(TEST_JSON_MAPPER.readValue(sampleSrsResponse, Record.class)));

    var importRequest = post("/linked-data/inventory-instance/{inventoryId}/import", instanceId)
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env));

    var payload = mockMvc.perform(importRequest)
      .andExpect(status().isCreated())
      .andReturn()
      .getResponse()
      .getContentAsString();

    var resourceId = TEST_JSON_MAPPER.readTree(payload).get("id").asString();

    mockMvc.perform(get("/linked-data/resource/{id}", resourceId)
        .contentType(APPLICATION_JSON)
        .headers(defaultHeaders(env)))
      .andExpect(status().isOk())
      .andExpect(jsonPath(toMediaCode()).value("s"))
      .andExpect(jsonPath(toMediaLink()).value("http://id.loc.gov/vocabulary/mediaTypes/s"))
      .andExpect(jsonPath(toMediaTerm()).value("computer"));
  }

  @Test
  void shouldImportMarc_andStoreCarrierCategory() throws Exception {
    var instanceId = UUID.randomUUID().toString();
    var sampleSrsResponse = """
      {
         "parsedRecord":{
            "content":{
               "fields":[
                  { "001":"in00000000003" },
                  {
                     "245":{
                        "ind1":" ",
                        "ind2":" ",
                        "subfields":[ { "a":"shouldImportMarc_andStoreCarrierCategory - Testing carrier type" } ]
                     }
                  },
                  {
                     "338":{
                        "ind1":" ",
                        "ind2":" ",
                        "subfields":[ { "a":"aperture card" }, { "b":"ha" } ]
                     }
                  }
               ],
               "leader":"00153nam a2200049uc 4500"
            }
         }
      }
      """;

    when(srsClient.getSourceStorageInstanceRecordById(instanceId))
      .thenReturn(ResponseEntity.ok(TEST_JSON_MAPPER.readValue(sampleSrsResponse, Record.class)));

    var importRequest = post("/linked-data/inventory-instance/{inventoryId}/import", instanceId)
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env));

    var payload = mockMvc.perform(importRequest)
      .andExpect(status().isCreated())
      .andReturn()
      .getResponse()
      .getContentAsString();

    var resourceId = TEST_JSON_MAPPER.readTree(payload).get("id").asString();

    mockMvc.perform(get("/linked-data/resource/{id}", resourceId)
        .contentType(APPLICATION_JSON)
        .headers(defaultHeaders(env)))
      .andExpect(status().isOk())
      .andExpect(jsonPath(toCarrierCode(toInstance())).value("ha"))
      .andExpect(jsonPath(toCarrierLink(toInstance())).value("http://id.loc.gov/vocabulary/carriers/ha"))
      .andExpect(jsonPath(toCarrierTerm(toInstance())).value("aperture card"));
  }

  @Test
  void shouldImportMarcAndEditResource() throws Exception {
    // Step 1: Import a MARC record
    var instanceId = UUID.randomUUID().toString();
    var sampleSrsResponse = """
      {
         "parsedRecord":{
            "content":{
               "fields":[
                  { "001":"in00000000005" },
                  { "008":"251031|                      ||| |      " },
                  {
                     "245":{
                        "ind1":" ",
                        "ind2":" ",
                        "subfields":[ { "a":"shouldImportMarcAndEditResource - Testing edit" } ]
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

    var importPayload = mockMvc.perform(
        post("/linked-data/inventory-instance/{inventoryId}/import", instanceId)
          .contentType(APPLICATION_JSON)
          .headers(defaultHeaders(env)))
      .andExpect(status().isCreated())
      .andReturn()
      .getResponse()
      .getContentAsString();

    var resourceId = TEST_JSON_MAPPER.readTree(importPayload).get("id").asString();

    // Step 2: GET the imported instance to retrieve the work reference ID
    var getResponse = mockMvc.perform(
        get("/linked-data/resource/{id}", resourceId)
          .contentType(APPLICATION_JSON)
          .headers(defaultHeaders(env)))
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString();

    var importedResponse = TEST_JSON_MAPPER.readValue(getResponse, ResourceResponseDto.class);
    var importedInstance = ((InstanceResponseField) importedResponse.getResource()).getInstance();
    var workId = Long.parseLong(importedInstance.getWorkReference().getFirst().getId());

    // Step 3: Edit the imported resource (simulates "Edit in Linked data editor" → Save)
    var specRuleId = UUID.randomUUID();
    when(specClient.getBibMarcSpecs()).thenReturn(ResponseEntity.ok(createSpecifications(specRuleId)));
    when(specClient.getSpecRules(specRuleId)).thenReturn(ResponseEntity.ok(createSpecRules()));

    searchWorkIndexTopicListener.getMessages().clear();

    var updateResponse = mockMvc.perform(
        put("/linked-data/resource/{id}", resourceId)
          .contentType(APPLICATION_JSON)
          .headers(defaultHeaders(env))
          .content(getInstanceRequestDto(workId, "shouldImportMarcAndEditResource - Edited instance")))
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString();

    // Step 4: Verify the edited resource — old instance replaced, source changed to LINKED_DATA
    var updatedResourceResponse = TEST_JSON_MAPPER.readValue(updateResponse, ResourceResponseDto.class);
    var updatedInstanceId = ((InstanceResponseField) updatedResourceResponse.getResource()).getInstance().getId();

    assertFalse(resourceTestService.existsById(Long.parseLong(resourceId)));
    var updatedResource = resourceTestService.getResourceById(updatedInstanceId, 1);
    assertThat(updatedResource.getFolioMetadata().getSource()).isEqualTo(LINKED_DATA);

    // Step 5: Editing the instance should publish an UPDATE event on the search index topic
    awaitAndAssert(() -> assertTrue(
      searchWorkIndexTopicListener.getMessages().stream()
        .anyMatch(msg -> {
          var root = TEST_JSON_MAPPER.readTree(msg);
          return root.get("type").asString().equals("UPDATE")
            && root.get("resourceName").asString().equals("linked-data-work")
            && msg.contains(String.valueOf(workId));
        })
    ));
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
