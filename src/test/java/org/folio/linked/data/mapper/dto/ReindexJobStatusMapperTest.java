package org.folio.linked.data.mapper.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.linked.data.configuration.batch.BatchConfig.REINDEX_STEP_NAME;
import static org.folio.linked.data.domain.dto.ReindexJobStatusDto.ReindexTypeEnum.FULL;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.stream.Stream;
import org.folio.linked.data.domain.dto.ReindexJobStatusDto;
import org.folio.linked.data.domain.dto.ReindexJobStatusDto.ReindexTypeEnum;
import org.folio.linked.data.model.entity.batch.BatchJobExecution;
import org.folio.linked.data.model.entity.batch.BatchStepExecution;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@UnitTest
class ReindexJobStatusMapperTest {

  private final ReindexJobStatusMapper mapper = new ReindexJobStatusMapperImpl();

  static Stream<Arguments> toDtoArgs() {
    var startTime = LocalDateTime.of(2026, 2, 27, 10, 0, 0);
    var endTime = LocalDateTime.of(2026, 2, 27, 10, 5, 0);

    return Stream.of(
      Arguments.of(
        "all fields mapped for full reindex",
        execution(startTime, endTime, "COMPLETED", "user-123", "true", Set.of(reindexStep(50000L, 49998L))),
        new ReindexJobStatusDto()
          .startDate(OffsetDateTime.of(startTime, ZoneOffset.UTC))
          .endDate(OffsetDateTime.of(endTime, ZoneOffset.UTC))
          .startedBy("user-123")
          .reindexType(FULL)
          .linesRead(50000L)
          .linesSent(49998L)
          .status("COMPLETED")
      ),
      Arguments.of(
        "incremental reindex type",
        execution(null, null, "STARTED", null, "false", Set.of()),
        new ReindexJobStatusDto().reindexType(ReindexTypeEnum.INCREMENTAL).linesRead(0L).linesSent(0L).status("STARTED")
      ),
      Arguments.of(
        "no params — null dates, startedBy, reindexType",
        execution(null, null, "STARTED", null, null, Set.of()),
        new ReindexJobStatusDto().linesRead(0L).linesSent(0L).status("STARTED")
      ),
      Arguments.of(
        "null steps — zero counts",
        execution(null, null, "STARTED", null, null, null),
        new ReindexJobStatusDto().linesRead(0L).linesSent(0L).status("STARTED")
      ),
      Arguments.of(
        "reindex step counts are null — zero counts",
        execution(null, null, "COMPLETED", null, null, Set.of(reindexStep(null, null))),
        new ReindexJobStatusDto().linesRead(0L).linesSent(0L).status("COMPLETED")
      ),
      Arguments.of(
        "non-reindex steps ignored",
        execution(null, null, "COMPLETED", null, null,
          Set.of(stepWithName("dropIndexStep", 999L, 999L), reindexStep(100L, 98L))),
        new ReindexJobStatusDto().linesRead(100L).linesSent(98L).status("COMPLETED")
      )
    );
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("toDtoArgs")
  void toDto(String description, BatchJobExecution execution, ReindexJobStatusDto expected) {
    assertThat(mapper.toDto(execution)).isEqualTo(expected);
  }

  private static BatchJobExecution execution(LocalDateTime startTime, LocalDateTime endTime,
                                              String status, String startedBy, String isFullReindex,
                                              Set<BatchStepExecution> steps) {
    var e = new BatchJobExecution();
    e.setStartTime(startTime);
    e.setEndTime(endTime);
    e.setStatus(status);
    e.setStartedBy(startedBy);
    e.setIsFullReindex(isFullReindex);
    e.setStepExecutions(steps);
    return e;
  }


  private static BatchStepExecution reindexStep(Long readCount, Long writeCount) {
    return stepWithName(1L, REINDEX_STEP_NAME, readCount, writeCount);
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
