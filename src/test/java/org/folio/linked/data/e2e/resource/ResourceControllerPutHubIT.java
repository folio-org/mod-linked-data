package org.folio.linked.data.e2e.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.PropertyDictionary.LINK;
import static org.folio.linked.data.domain.dto.ResourceIndexEventType.CREATE;
import static org.folio.linked.data.domain.dto.ResourceIndexEventType.DELETE;
import static org.folio.linked.data.e2e.resource.ResourceControllerITBase.RESOURCE_URL;
import static org.folio.linked.data.test.TestUtil.TEST_JSON_MAPPER;
import static org.folio.linked.data.test.TestUtil.awaitAndAssert;
import static org.folio.linked.data.test.TestUtil.defaultHeaders;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.domain.dto.ResourceIndexEventType;
import org.folio.linked.data.e2e.ITBase;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.repo.ResourceRepository;
import org.folio.linked.data.test.kafka.KafkaProducerTestConfiguration;
import org.folio.linked.data.test.kafka.KafkaSearchHubIndexTopicListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
@SpringBootTest(classes = {KafkaProducerTestConfiguration.class})
class ResourceControllerPutHubIT extends ITBase {

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private KafkaSearchHubIndexTopicListener searchIndexTopicListener;
  @Autowired
  private ResourceRepository resourceRepository;

  @BeforeEach
  @Override
  public void beforeEach() {
    super.beforeEach();
    searchIndexTopicListener.getMessages().clear();
  }

  @Test
  void shouldUpdateHubAndEmitSearchIndexEvents() throws Exception {
    var oldLink = "https://example.org/hub-link";
    var oldDoc = TEST_JSON_MAPPER.createObjectNode();
    oldDoc.putArray(LINK.getValue()).add(oldLink);
    var existingHub = new Resource()
      .setIdAndRefreshEdges(1L)
      .addTypes(ResourceTypeDictionary.HUB)
      .setDoc(oldDoc);
    resourceRepository.save(existingHub);

    var putRequest = put(RESOURCE_URL + "/" + existingHub.getId())
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env))
      .content("""
        {
          "resource": {
            "http://bibfra.me/vocab/lite/Hub": {
              "profileId": 3,
              "http://bibfra.me/vocab/library/title": [
                {
                  "http://bibfra.me/vocab/library/Title": {
                    "http://bibfra.me/vocab/library/mainTitle": [ "HUB TEST TITLE" ]
                  }
                }
              ]
            }
          }
        }"""
      );

    // when
    var resultActions = mockMvc.perform(putRequest);

    // then
    var responseJson = resultActions
      .andExpect(status().isOk())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andReturn().getResponse()
      .getContentAsString();

    var hubIdStr = TEST_JSON_MAPPER.readTree(responseJson)
      .path("resource")
      .path("http://bibfra.me/vocab/lite/Hub")
      .path("id")
      .asString();
    checkSearchIndexMessage(existingHub.getId(), DELETE);
    var newHubId = Long.parseLong(hubIdStr);
    checkSearchIndexMessage(newHubId, CREATE);
    checkIndexDate(hubIdStr);

    var updatedHub = resourceRepository.findById(newHubId).orElseThrow();
    assertThat(updatedHub.getDoc().has(LINK.getValue())).isTrue();
    assertThat(updatedHub.getDoc().get(LINK.getValue())).isNotEmpty();
    assertThat(updatedHub.getDoc().get(LINK.getValue()).get(0).asString()).isEqualTo(oldLink);
  }

  protected void checkSearchIndexMessage(Long id, ResourceIndexEventType eventType) {
    awaitAndAssert(() ->
      assertThat(searchIndexTopicListener.getMessages().stream().anyMatch(m -> m.contains(id.toString())
        && m.contains(eventType.getValue()))).isTrue()
    );
  }

  protected void checkIndexDate(String id) {
    assertThat(resourceTestService.getResourceById(id, 0).getIndexDate()).isNotNull();
  }
}
