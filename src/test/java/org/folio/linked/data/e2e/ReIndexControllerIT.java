package org.folio.linked.data.e2e;

import static java.util.Objects.nonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.linked.data.domain.dto.ResourceIndexEventType.CREATE;
import static org.folio.linked.data.test.MonographTestUtil.getSampleWork;
import static org.folio.linked.data.test.TestUtil.TENANT_ID;
import static org.folio.linked.data.test.TestUtil.awaitAndAssert;
import static org.folio.linked.data.test.TestUtil.cleanResourceTables;
import static org.folio.linked.data.test.TestUtil.defaultHeaders;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Date;
import lombok.SneakyThrows;
import org.folio.linked.data.e2e.base.IntegrationTestFolio;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.repo.ResourceEdgeRepository;
import org.folio.linked.data.repo.ResourceRepository;
import org.folio.linked.data.service.tenant.TenantScopedExecutionService;
import org.folio.linked.data.test.kafka.KafkaSearchWorkIndexTopicListener;
import org.folio.spring.tools.kafka.KafkaAdminService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTestFolio
class ReIndexControllerIT {

  public static final String INDEX_URL = "/linked-data/reindex";

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ResourceRepository resourceRepo;
  @Autowired
  private ResourceEdgeRepository resourceEdgeRepository;
  @Autowired
  private Environment env;
  @Autowired
  private KafkaSearchWorkIndexTopicListener consumer;
  @Autowired
  private JdbcTemplate jdbcTemplate;
  @Autowired
  private TenantScopedExecutionService tenantScopedExecutionService;

  @BeforeAll
  static void beforeAll(@Autowired KafkaAdminService kafkaAdminService) {
    kafkaAdminService.createTopics(TENANT_ID);
  }

  @BeforeEach
  public void beforeEach() {
    tenantScopedExecutionService.execute(TENANT_ID, () ->
      cleanResourceTables(jdbcTemplate)
    );
    consumer.getMessages().clear();
  }

  @Test
  void indexResourceWithNoIndexDate_andNotFullIndexRequest() throws Exception {
    // given
    var work = resourceRepo.save(getSampleWork(null));

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
    resourceRepo.save(getSampleWork(null).setIndexDate(new Date()));

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
