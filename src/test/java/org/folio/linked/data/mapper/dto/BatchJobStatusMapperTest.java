package org.folio.linked.data.mapper.dto;

import static java.time.OffsetDateTime.of;
import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.linked.data.configuration.batch.graph.GraphCleaningBatchJobConfig.GRAPH_CLEANING_STEP_NAME;
import static org.folio.linked.data.configuration.batch.reindex.ReindexBatchJobConfig.REINDEX_STEP_NAME;
import static org.folio.linked.data.domain.dto.BatchJobStatusDto.ReindexTypeEnum.FULL;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Stream;
import org.folio.linked.data.domain.dto.BatchJobStatusDto;
import org.folio.linked.data.domain.dto.BatchJobStatusDto.ReindexTypeEnum;
import org.folio.linked.data.model.entity.batch.BatchJobExecution;
import org.folio.linked.data.model.entity.batch.BatchStepExecution;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@UnitTest
class BatchJobStatusMapperTest {

  private final BatchJobStatusMapper mapper = new BatchJobStatusMapperImpl();

  static Stream<Arguments> toDtoArgs() {
    var startTime = LocalDateTime.of(2026, 2, 27, 10, 0, 0);
    var endTime = LocalDateTime.of(2026, 2, 27, 10, 5, 0);

    return Stream.of(
      Arguments.of(
        "all fields mapped for full reindex",
        REINDEX_STEP_NAME,
        execution(startTime, endTime, "COMPLETED", "user-123", "true", null, Set.of(reindexStep(50000L, 49998L))),
        new BatchJobStatusDto()
          .startDate(of(startTime, UTC))
          .endDate(of(endTime, UTC))
          .startedBy("user-123")
          .reindexType(FULL)
          .linesRead(50000L)
          .linesSent(49998L)
          .status("COMPLETED")
      ),
      Arguments.of(
        "incremental reindex type",
        REINDEX_STEP_NAME,
        execution(null, null, "STARTED", null, "false", null, Set.of()),
        new BatchJobStatusDto().reindexType(ReindexTypeEnum.INCREMENTAL).linesRead(0L).linesSent(0L).status("STARTED")
      ),
      Arguments.of(
        "no params — null dates, startedBy, reindexType",
        REINDEX_STEP_NAME,
        execution(null, null, "STARTED", null, null, null, Set.of()),
        new BatchJobStatusDto().linesRead(0L).linesSent(0L).status("STARTED")
      ),
      Arguments.of(
        "null steps — zero counts",
        REINDEX_STEP_NAME,
        execution(null, null, "STARTED", null, null, null, null),
        new BatchJobStatusDto().linesRead(0L).linesSent(0L).status("STARTED")
      ),
      Arguments.of(
        "reindex step counts are null — zero counts",
        REINDEX_STEP_NAME,
        execution(null, null, "COMPLETED", null, null, null, Set.of(reindexStep(null, null))),
        new BatchJobStatusDto().linesRead(0L).linesSent(0L).status("COMPLETED")
      ),
      Arguments.of(
        "non-reindex steps ignored",
        REINDEX_STEP_NAME,
        execution(null, null, "COMPLETED", null, null, null,
          Set.of(stepWithName("dropIndexStep", 999L, 999L), reindexStep(100L, 98L))),
        new BatchJobStatusDto().linesRead(100L).linesSent(98L).status("COMPLETED")
      ),
      Arguments.of(
        "graph cleaning — all fields mapped",
        GRAPH_CLEANING_STEP_NAME,
        execution(startTime, endTime, "COMPLETED", null, null, 2,
          Set.of(graphCleaningStep(1000L, 999L))),
        new BatchJobStatusDto().startDate(of(startTime, UTC)).endDate(of(endTime, UTC))
          .executionRound(2).linesRead(1000L).linesSent(999L).status("COMPLETED")
      ),
      Arguments.of(
        "graph cleaning — non-graph steps ignored",
        GRAPH_CLEANING_STEP_NAME,
        execution(null, null, "COMPLETED", null, null, 1,
          Set.of(stepWithName("dropIndexStep", 999L, 999L), graphCleaningStep(100L, 98L))),
        new BatchJobStatusDto().executionRound(1).linesRead(100L).linesSent(98L).status("COMPLETED")
      ),
      Arguments.of(
        "graph cleaning — null steps, zero counts",
        GRAPH_CLEANING_STEP_NAME,
        execution(null, null, "STARTED", null, null, 0, null),
        new BatchJobStatusDto().executionRound(0).linesRead(0L).linesSent(0L).status("STARTED")
      )
    );
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("toDtoArgs")
  void toDto(String description, String stepName, BatchJobExecution execution, BatchJobStatusDto expected) {
    assertThat(mapper.toDto(execution, stepName)).isEqualTo(expected);
  }

  private static BatchJobExecution execution(LocalDateTime startTime, LocalDateTime endTime,
                                              String status, String startedBy, String isFullReindex,
                                              Integer executionRound, Set<BatchStepExecution> steps) {
    var e = new BatchJobExecution();
    e.setStartTime(startTime);
    e.setEndTime(endTime);
    e.setStatus(status);
    e.setStartedBy(startedBy);
    e.setIsFullReindex(isFullReindex);
    e.setExecutionRound(executionRound);
    e.setStepExecutions(steps);
    return e;
  }

  private static BatchStepExecution reindexStep(Long readCount, Long writeCount) {
    return stepWithName(1L, REINDEX_STEP_NAME, readCount, writeCount);
  }

  private static BatchStepExecution graphCleaningStep(Long readCount, Long writeCount) {
    return stepWithName(1L, GRAPH_CLEANING_STEP_NAME, readCount, writeCount);
  }

  private static BatchStepExecution stepWithName(String name, Long readCount, Long writeCount) {
    return stepWithName(2L, name, readCount, writeCount);
  }

  private static BatchStepExecution stepWithName(Long id, String name, Long readCount, Long writeCount) {
    var s = new BatchStepExecution();
    s.setStepExecutionId(id);
    s.setStepName(name);
    s.setReadCount(readCount);
    s.setWriteCount(writeCount);
    return s;
  }
}
