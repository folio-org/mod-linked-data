package org.folio.linked.data.mapper.dto;

import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static org.folio.linked.data.domain.dto.BatchJobStatusDto.ReindexTypeEnum.FULL;
import static org.folio.linked.data.domain.dto.BatchJobStatusDto.ReindexTypeEnum.INCREMENTAL;
import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Set;
import org.folio.linked.data.domain.dto.BatchJobStatusDto;
import org.folio.linked.data.model.entity.batch.BatchJobExecution;
import org.folio.linked.data.model.entity.batch.BatchStepExecution;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = SPRING)
public interface BatchJobStatusMapper {

  @Mapping(target = "startDate", source = "execution.startTime")
  @Mapping(target = "endDate", source = "execution.endTime")
  @Mapping(target = "reindexType", source = "execution.isFullReindex")
  @Mapping(target = "linesRead", expression = "java(linesRead(execution.getStepExecutions(), stepName))")
  @Mapping(target = "linesSent", expression = "java(linesSent(execution.getStepExecutions(), stepName))")
  BatchJobStatusDto toDto(BatchJobExecution execution, String jobName, String stepName);

  default OffsetDateTime toOffsetDateTime(LocalDateTime localDateTime) {
    return ofNullable(localDateTime).map(ldt -> ldt.atOffset(ZoneOffset.UTC)).orElse(null);
  }

  default BatchJobStatusDto.ReindexTypeEnum toReindexType(String isFullReindex) {
    return ofNullable(isFullReindex)
      .map(Boolean::parseBoolean)
      .map(full -> full ? FULL : INCREMENTAL)
      .orElse(null);
  }

  default Long linesRead(Set<BatchStepExecution> steps, String stepName) {
    return getLinesCount(steps, stepName, true);
  }

  default Long linesSent(Set<BatchStepExecution> steps, String stepName) {
    return getLinesCount(steps, stepName, false);
  }

  private Long getLinesCount(Set<BatchStepExecution> steps, String stepName, boolean read) {
    if (isNull(steps)) {
      return 0L;
    }
    return steps.stream()
      .filter(s -> stepName.equals(s.getStepName()))
      .mapToLong(s -> read
        ? ofNullable(s.getReadCount()).orElse(0L)
        : ofNullable(s.getWriteCount()).orElse(0L))
      .sum();
  }
}
