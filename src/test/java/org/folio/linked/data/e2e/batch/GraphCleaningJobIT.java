package org.folio.linked.data.e2e.batch;

import static java.util.Collections.emptyMap;
import static java.util.Set.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.PredicateDictionary.STATUS;
import static org.folio.ld.dictionary.ResourceTypeDictionary.IDENTIFIER;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PERSON;
import static org.folio.linked.data.test.MonographTestUtil.createResource;
import static org.folio.linked.data.test.MonographTestUtil.getSampleHub;
import static org.folio.linked.data.test.MonographTestUtil.getSampleInstanceResource;
import static org.folio.linked.data.test.TestUtil.TENANT_ID;
import static org.folio.linked.data.test.TestUtil.awaitAndAssert;
import static org.folio.linked.data.test.TestUtil.defaultHeaders;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.e2e.base.ITBase;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.linked.data.model.entity.FolioMetadata;
import org.folio.linked.data.repo.ResourceRepository;
import org.folio.linked.data.service.batch.BatchJobService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
class GraphCleaningJobIT extends ITBase {

  private static final String BATCH_STATUS_URL = "/linked-data/batch/status";

  @Autowired
  private BatchJobService batchJobService;
  @Autowired
  private ResourceRepository resourceRepository;

  @Test
  void graphCleaningJob_shouldDeleteOrphanResourcesOnly() throws Exception {
    // given
    var savedIds = tenantScopedExecutionService.execute(TENANT_ID, () -> {
      resourceTestService.saveGraph(getSampleInstanceResource());
      resourceTestService.saveGraph(getSampleHub());

      var personWithMeta = createResource(Map.of(), of(PERSON), emptyMap())
        .setLabel("personWithMeta");
      var folioMetadata = new FolioMetadata(personWithMeta).setInventoryId("some-inventory-id");
      personWithMeta.setFolioMetadata(folioMetadata);
      resourceTestService.saveGraph(personWithMeta);

      var orphanPerson = resourceTestService.saveGraph(
        createResource(Map.of(), of(PERSON), emptyMap()).setLabel("personWithoutMeta")
      );

      var orphanIdentifierWithStatus = resourceTestService.saveGraph(
        createResource(Map.of(), of(IDENTIFIER), Map.of(STATUS, List.of(
          createResource(Map.of(), of(ResourceTypeDictionary.STATUS), emptyMap()).setLabel("orphanStatus")
        ))).setLabel("orphanIdentifier")
      );

      return new long[]{orphanPerson.getId(), orphanIdentifierWithStatus.getId()};
    });

    var orphanPersonId = savedIds[0];
    var orphanIdentifierId = savedIds[1];

    // when
    tenantScopedExecutionService.execute(TENANT_ID,
      () -> batchJobService.startGraphCleaning(1)
    );

    // then
    awaitAllRoundsCompleted();

    tenantScopedExecutionService.execute(TENANT_ID, () -> {
      assertThat(resourceRepository.findById(orphanPersonId)).isEmpty();
      assertThat(resourceRepository.findById(orphanIdentifierId)).isEmpty();
      return null;
    });

    var executionIds = getGraphCleaningExecutionIds();
    assertThat(executionIds).hasSize(3);

    assertJobRound(executionIds.get(0), 1, 2, 2);
    assertJobRound(executionIds.get(1), 2, 1, 1);
    assertJobRound(executionIds.get(2), 3, 0, 0);
  }

  private void assertJobRound(long jobExecutionId, int expectedRound,
                              long expectedLinesRead, long expectedLinesSent) throws Exception {
    mockMvc.perform(get(BATCH_STATUS_URL)
        .param("jobExecutionId", String.valueOf(jobExecutionId))
        .headers(defaultHeaders(env)))
      .andExpect(status().isOk())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andExpect(jsonPath("$.jobName").value("graphCleaningJob"))
      .andExpect(jsonPath("$.status").value("COMPLETED"))
      .andExpect(jsonPath("$.startDate").isNotEmpty())
      .andExpect(jsonPath("$.endDate").isNotEmpty())
      .andExpect(jsonPath("$.executionRound").value(expectedRound))
      .andExpect(jsonPath("$.linesRead").value(expectedLinesRead))
      .andExpect(jsonPath("$.linesSent").value(expectedLinesSent))
      .andExpect(jsonPath("$.reindexType").doesNotExist())
      .andExpect(jsonPath("$.startedBy").doesNotExist());
  }

  private void awaitAllRoundsCompleted() {
    awaitAndAssert(() -> {
      var executionIds = getGraphCleaningExecutionIds();
      assertThat(executionIds).hasSizeGreaterThanOrEqualTo(3);

      var lastId = executionIds.getLast();
      var lastStatus = tenantScopedExecutionService.execute(TENANT_ID, () ->
        jdbcTemplate.queryForObject(
          "SELECT STATUS FROM batch_job_execution WHERE JOB_EXECUTION_ID = ?",
          String.class, lastId
        )
      );
      assertThat(lastStatus).isEqualTo("COMPLETED");

      var lastRoundWriteCount = tenantScopedExecutionService.execute(TENANT_ID, () ->
        jdbcTemplate.queryForObject(
          """
            SELECT COALESCE(SUM(s.WRITE_COUNT), 0)
            FROM batch_step_execution s
            WHERE s.JOB_EXECUTION_ID = ?
            """,
          Long.class, lastId
        )
      );
      assertThat(lastRoundWriteCount).isZero();
    });
  }

  private List<Long> getGraphCleaningExecutionIds() {
    return tenantScopedExecutionService.execute(TENANT_ID, () ->
      jdbcTemplate.queryForList(
        """
          SELECT e.JOB_EXECUTION_ID
          FROM batch_job_execution e
          JOIN batch_job_instance i ON e.JOB_INSTANCE_ID = i.JOB_INSTANCE_ID
          WHERE i.JOB_NAME = 'graphCleaningJob'
          ORDER BY e.JOB_EXECUTION_ID
          """,
        Long.class
      )
    );
  }
}


