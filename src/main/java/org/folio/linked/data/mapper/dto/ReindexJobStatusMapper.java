package org.folio.linked.data.mapper.dto;

import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static org.folio.linked.data.configuration.batch.BatchConfig.REINDEX_STEP_NAME;
import static org.folio.linked.data.domain.dto.ReindexJobStatusDto.ReindexTypeEnum.FULL;
import static org.folio.linked.data.domain.dto.ReindexJobStatusDto.ReindexTypeEnum.INCREMENTAL;
import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Set;
import org.folio.linked.data.domain.dto.ReindexJobStatusDto;
import org.folio.linked.data.domain.dto.ReindexJobStatusDto.ReindexTypeEnum;
import org.folio.linked.data.model.entity.batch.BatchJobExecution;
import org.folio.linked.data.model.entity.batch.BatchStepExecution;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = SPRING)
public interface ReindexJobStatusMapper {

  @Mapping(target = "startDate", source = "startTime")
  @Mapping(target = "endDate", source = "endTime")
  @Mapping(target = "reindexType", source = "isFullReindex")
  @Mapping(target = "linesRead", expression = "java(linesRead(execution.getStepExecutions()))")
  @Mapping(target = "linesSent", expression = "java(linesSent(execution.getStepExecutions()))")
  ReindexJobStatusDto toDto(BatchJobExecution execution);

  default OffsetDateTime toOffsetDateTime(LocalDateTime localDateTime) {
    return ofNullable(localDateTime).map(ldt -> ldt.atOffset(ZoneOffset.UTC)).orElse(null);
  }

  default ReindexTypeEnum toReindexType(String isFullReindex) {
    return ofNullable(isFullReindex)
      .map(Boolean::parseBoolean)
      .map(full -> full ? FULL : INCREMENTAL)
      .orElse(null);
  }

  default Long linesRead(Set<BatchStepExecution> steps) {
    return getReindexStepCount(steps, true);
  }

  default Long linesSent(Set<BatchStepExecution> steps) {
    return getReindexStepCount(steps, false);
  }

  private Long getReindexStepCount(Set<BatchStepExecution> steps, boolean read) {
    if (isNull(steps)) {
      return 0L;
    }
    return steps.stream()
      .filter(s -> REINDEX_STEP_NAME.equals(s.getStepName()))
      .findFirst()
      .map(s -> read
        ? ofNullable(s.getReadCount()).orElse(0L)
        : ofNullable(s.getWriteCount()).orElse(0L))
      .orElse(0L);
  }
}
