package org.folio.linked.data.e2e.resource;

import static org.folio.linked.data.e2e.resource.ResourceControllerITBase.RESOURCE_URL;
import static org.folio.linked.data.test.MonographTestUtil.getSampleInstanceResource;
import static org.folio.linked.data.test.MonographTestUtil.getWork;
import static org.folio.linked.data.test.TestUtil.TEST_JSON_MAPPER;
import static org.folio.linked.data.test.TestUtil.awaitAndAssert;
import static org.folio.linked.data.test.TestUtil.defaultHeaders;
import static org.folio.linked.data.test.TestUtil.getInstanceRequestDto;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.folio.linked.data.e2e.base.ITBase;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.linked.data.test.kafka.KafkaProducerTestConfiguration;
import org.folio.linked.data.test.kafka.KafkaSearchWorkIndexTopicListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@IntegrationTest
@SpringBootTest(classes = {KafkaProducerTestConfiguration.class})
class ResourceControllerCreateInstanceIT extends ITBase {

  @Autowired
  private KafkaSearchWorkIndexTopicListener workIndexTopicListener;

  @BeforeEach
  @Override
  public void beforeEach() {
    super.beforeEach();
    workIndexTopicListener.getMessages().clear();
  }

  @Test
  void createInstance_shouldIncludeBothInstancesInWorkSearchIndexMessage_whenSecondInstanceAddedToExistingWork()
    throws Exception {
    // given
    var work = getWork("simple_work", hashService);
    var instance1 = getSampleInstanceResource(null, work);
    resourceTestService.saveGraph(instance1);

    var createRequest = post(RESOURCE_URL)
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env))
      .content(getInstanceRequestDto(work.getId(), "instance2_title"));

    // when
    var response = mockMvc.perform(createRequest)
      .andExpect(status().isOk())
      .andReturn().getResponse().getContentAsString();

    // then
    var instance2Id = TEST_JSON_MAPPER.readTree(response)
      .path("resource")
      .path("http://bibfra.me/vocab/lite/Instance")
      .path("id")
      .asLong();
    awaitAndAssert(() ->
      assertTrue(workIndexTopicListener.getMessages().stream()
        .anyMatch(m -> m.contains(work.getId().toString())
          && m.contains(instance1.getId().toString())
          && m.contains(String.valueOf(instance2Id))
          && m.contains("\"titles\"")))
    );
  }
}
