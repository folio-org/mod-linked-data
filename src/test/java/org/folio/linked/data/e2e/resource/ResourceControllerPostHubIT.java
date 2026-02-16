package org.folio.linked.data.e2e.resource;

import static org.folio.linked.data.domain.dto.ResourceIndexEventType.CREATE;
import static org.folio.linked.data.e2e.resource.ResourceControllerITBase.RESOURCE_URL;
import static org.folio.linked.data.test.TestUtil.TEST_JSON_MAPPER;
import static org.folio.linked.data.test.TestUtil.awaitAndAssert;
import static org.folio.linked.data.test.TestUtil.defaultHeaders;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.folio.linked.data.domain.dto.ResourceIndexEventType;
import org.folio.linked.data.e2e.ITBase;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.linked.data.test.kafka.KafkaProducerTestConfiguration;
import org.folio.linked.data.test.kafka.KafkaSearchHubIndexTopicListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
@SpringBootTest(classes = {KafkaProducerTestConfiguration.class})
class ResourceControllerPostHubIT extends ITBase {

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private KafkaSearchHubIndexTopicListener searchIndexTopicListener;

  @BeforeEach
  @Override
  public void beforeEach() {
    super.beforeEach();
    searchIndexTopicListener.getMessages().clear();
  }

  @Test
  void shouldCreateHubAndEmitSearchIndexEvent() throws Exception {
    var postRequest = post(RESOURCE_URL)
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
    var resultActions = mockMvc.perform(postRequest);

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
    checkSearchIndexMessage(Long.parseLong(hubIdStr), CREATE);
    checkIndexDate(hubIdStr);
  }

  protected void checkSearchIndexMessage(Long id, ResourceIndexEventType eventType) {
    awaitAndAssert(() ->
      assertTrue(searchIndexTopicListener.getMessages().stream().anyMatch(m -> m.contains(id.toString())
        && m.contains(eventType.getValue())))
    );
  }

  protected void checkIndexDate(String id) {
    assertNotNull(resourceTestService.getResourceById(id, 0).getIndexDate());
  }
}
