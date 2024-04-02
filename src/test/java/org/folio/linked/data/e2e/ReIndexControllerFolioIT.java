package org.folio.linked.data.e2e;

import static java.util.Objects.nonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.linked.data.test.MonographTestUtil.getSampleInstanceResource;
import static org.folio.linked.data.test.MonographTestUtil.getSampleWork;
import static org.folio.linked.data.test.TestUtil.FOLIO_TEST_PROFILE;
import static org.folio.linked.data.test.TestUtil.TENANT_ID;
import static org.folio.linked.data.test.TestUtil.awaitAndAssert;
import static org.folio.linked.data.test.TestUtil.defaultHeaders;
import static org.folio.linked.data.test.TestUtil.randomLong;
import static org.folio.linked.data.util.Constants.FOLIO_PROFILE;
import static org.folio.linked.data.util.Constants.SEARCH_PROFILE;
import static org.folio.search.domain.dto.ResourceEventType.CREATE;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Date;
import lombok.SneakyThrows;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.repo.ResourceRepository;
import org.folio.linked.data.test.kafka.KafkaSearchIndexTopicListener;
import org.folio.spring.tools.kafka.KafkaAdminService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
@ActiveProfiles({FOLIO_PROFILE, FOLIO_TEST_PROFILE, SEARCH_PROFILE})
class ReIndexControllerFolioIT {

  public static final String INDEX_URL = "/reindex";

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ResourceRepository resourceRepo;
  @Autowired
  private Environment env;
  @Autowired
  private KafkaSearchIndexTopicListener consumer;

  @BeforeAll
  static void beforeAll(@Autowired KafkaAdminService kafkaAdminService) {
    kafkaAdminService.createTopics(TENANT_ID);
  }

  @Test
  void indexResourceWithNoIndexDate_andNotFullIndexRequest() throws Exception {
    // given
    var work = resourceRepo.save(getSampleWork(null));
    var instance = resourceRepo.save(getSampleInstanceResource(null, work));
    var anotherWork = getSampleWork(instance);
    anotherWork.setId(randomLong());
    resourceRepo.save(anotherWork);

    var requestBuilder = put(INDEX_URL)
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env));

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    resultActions.andExpect(status().isNoContent());
    checkKafkaMessageSent(work);
  }

  @Test
  void notIndexResourceWithIndexDate_andNotFullIndexRequest() throws Exception {
    // given
    var work = resourceRepo.save(getSampleWork(null).setIndexDate(new Date()));
    resourceRepo.save(getSampleInstanceResource(null, work));

    var requestBuilder = put(INDEX_URL)
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env));

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    resultActions.andExpect(status().isNoContent());
    checkKafkaMessageSent(null);
  }

  @Test
  void indexResourceWithNoIndexDate_andFullIndexRequest() throws Exception {
    // given
    var work = resourceRepo.save(getSampleWork(null));
    resourceRepo.save(getSampleInstanceResource(null, work));

    var requestBuilder = put(INDEX_URL)
      .param("full", "true")
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env));

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    resultActions.andExpect(status().isNoContent());
    checkKafkaMessageSent(work);
  }

  @Test
  void indexResourceWithIndexDate_andFullIndexRequest() throws Exception {
    // given
    var work = resourceRepo.save(getSampleWork(null).setIndexDate(new Date()));
    resourceRepo.save(getSampleInstanceResource(null, work));

    var requestBuilder = put(INDEX_URL)
      .param("full", "true")
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env));

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    resultActions.andExpect(status().isNoContent());
    checkKafkaMessageSent(work);
  }

  @SneakyThrows
  protected void checkKafkaMessageSent(Resource indexed) {
    if (nonNull(indexed)) {
      awaitAndAssert(() -> assertTrue(consumer.getMessages()
        .stream()
        .anyMatch(m -> m.contains(indexed.getId().toString()) && m.contains(CREATE.getValue()))));
      var freshPersistedOptional = resourceRepo.findById(indexed.getId());
      assertThat(freshPersistedOptional).isPresent();
      var freshPersisted = freshPersistedOptional.get();
      assertThat(freshPersisted.getIndexDate()).isNotNull();
    } else {
      awaitAndAssert(() -> assertTrue(consumer.getMessages().isEmpty()));
    }
  }

}
