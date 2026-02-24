package org.folio.linked.data.e2e;

import static java.lang.Long.parseLong;
import static java.time.ZoneId.systemDefault;
import static java.util.Objects.nonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.ResourceTypeDictionary.HUB;
import static org.folio.linked.data.domain.dto.ResourceIndexEventType.CREATE;
import static org.folio.linked.data.test.MonographTestUtil.getSampleHub;
import static org.folio.linked.data.test.MonographTestUtil.getSampleWork;
import static org.folio.linked.data.test.TestUtil.TENANT_ID;
import static org.folio.linked.data.test.TestUtil.awaitAndAssert;
import static org.folio.linked.data.test.TestUtil.defaultHeaders;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.Date;
import java.util.stream.Stream;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.repo.ResourceRepository;
import org.folio.linked.data.test.kafka.KafkaSearchHubIndexTopicListener;
import org.folio.linked.data.test.kafka.KafkaSearchWorkIndexTopicListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
class ReIndexControllerIT extends ITBase {

  private static final String FULL_REINDEX_URL = "/linked-data/reindex/full";
  private static final String INCREMENTAL_REINDEX_URL = "/linked-data/reindex/incremental";
  private static final String QUERY_PARAM_RESOURCE_TYPE = "resourceType";
  private static final Date GIVEN_INDEX_DATE = Date.from(LocalDate.of(1986, 9, 15)
    .atStartOfDay(systemDefault()).toInstant());

  @Autowired
  private ResourceRepository resourceRepo;
  @Autowired
  private KafkaSearchWorkIndexTopicListener workConsumer;
  @Autowired
  private KafkaSearchHubIndexTopicListener hubConsumer;

  @BeforeEach
  @Override
  public void beforeEach() {
    super.beforeEach();
    workConsumer.getMessages().clear();
    hubConsumer.getMessages().clear();
  }

  @ParameterizedTest
  @MethodSource("reindexTestCases")
  void reindex_shouldReindexResourcesAccordingToParameters(boolean isFullReindex, String resourceType,
                                                           boolean hasIndexDate, boolean expectWorkIndexed,
                                                           boolean expectHubIndexed) throws Exception {
    //given
    var url = isFullReindex ? FULL_REINDEX_URL : INCREMENTAL_REINDEX_URL;
    var requestBuilder = post(url).headers(defaultHeaders(env));
    if (nonNull(resourceType)) {
      requestBuilder.queryParam(QUERY_PARAM_RESOURCE_TYPE, resourceType);
    }
    var work = resourceTestService.saveGraph(getSampleWork().setIndexDate(hasIndexDate ? GIVEN_INDEX_DATE : null));
    var hub = resourceTestService.saveGraph(getSampleHub().setIndexDate(hasIndexDate ? GIVEN_INDEX_DATE : null));

    //when
    var resultActions = mockMvc.perform(requestBuilder);

    //then
    var jobInstanceId = parseLong(resultActions
      .andExpect(status().isOk())
      .andReturn().getResponse().getContentAsString()
    );
    awaitJobCompletion(jobInstanceId);
    assertIndexMessageSent(work, expectWorkIndexed);
    assertIndexMessageSent(hub, expectHubIndexed);
    assertIndexDateSet(work, expectWorkIndexed);
    assertIndexDateSet(hub, expectHubIndexed);
  }

  private static Stream<Arguments> reindexTestCases() {
    return Stream.of(
      // isFullReindex, resourceType, hasIndexDate, expectWorkIndexed, expectHubIndexed
      Arguments.of(true, null, false, true, true),
      Arguments.of(true, null, true, true, true),
      Arguments.of(true, "WORK", false, true, false),
      Arguments.of(true, "WORK", true, true, false),
      Arguments.of(true, "HUB", false, false, true),
      Arguments.of(true, "HUB", true, false, true),
      Arguments.of(false, null, false, true, true),
      Arguments.of(false, null, true, false, false),
      Arguments.of(false, "WORK", false, true, false),
      Arguments.of(false, "WORK", true, false, false),
      Arguments.of(false, "HUB", false, false, true),
      Arguments.of(false, "HUB", true, false, false)
    );
  }

  private void awaitJobCompletion(Long jobInstanceId) {
    awaitAndAssert(() -> {
      var status = tenantScopedExecutionService.execute(TENANT_ID, () -> jdbcTemplate.queryForObject(
        "SELECT STATUS FROM batch_job_execution WHERE JOB_INSTANCE_ID = ? ORDER BY JOB_EXECUTION_ID DESC LIMIT 1",
        String.class,
        jobInstanceId
      ));
      assertEquals("COMPLETED", status);
    });
  }

  private void assertIndexMessageSent(Resource resource, boolean isSent) {
    if (isSent) {
      if (resource.isOfType(HUB)) {
        assertTrue(hubConsumer.getMessages()
          .stream()
          .anyMatch(m -> m.contains(resource.getId().toString()) && m.contains(CREATE.getValue())));
      } else {
        assertTrue(workConsumer.getMessages()
          .stream()
          .anyMatch(m -> m.contains(resource.getId().toString()) && m.contains(CREATE.getValue())));
      }
    } else {
      if (resource.isOfType(HUB)) {
        assertTrue(hubConsumer.getMessages()
          .stream()
          .noneMatch(m -> m.contains(resource.getId().toString()) && m.contains(CREATE.getValue())));
      } else {
        assertTrue(workConsumer.getMessages()
          .stream()
          .noneMatch(m -> m.contains(resource.getId().toString()) && m.contains(CREATE.getValue())));
      }
    }
  }

  private void assertIndexDateSet(Resource resource, boolean isSet) {
    var freshPersistedOptional = resourceRepo.findById(resource.getId());
    assertThat(freshPersistedOptional).isPresent();
    var indexDate = freshPersistedOptional.get().getIndexDate();
    if (isSet) {
      assertThat(indexDate).isNotNull();
      assertThat(indexDate.getTime()).isNotEqualTo(GIVEN_INDEX_DATE.getTime());
    } else if (nonNull(indexDate)) {
      assertThat(indexDate.getTime()).isEqualTo(GIVEN_INDEX_DATE.getTime());
    }
  }

}
