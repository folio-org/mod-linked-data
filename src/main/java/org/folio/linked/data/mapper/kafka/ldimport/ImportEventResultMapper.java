package org.folio.linked.data.mapper.kafka.ldimport;

import static org.folio.linked.data.util.ImportUtils.ImportReport;
import static org.folio.linked.data.util.ImportUtils.ImportedResource;
import static org.folio.linked.data.util.ImportUtils.Status;
import static org.folio.linked.data.util.ImportUtils.Status.FAILED;
import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.domain.dto.FailedResource;
import org.folio.linked.data.domain.dto.ImportOutputEvent;
import org.folio.linked.data.domain.dto.ImportResultEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Log4j2
@Mapper(componentModel = SPRING, imports = Status.class)
public abstract class ImportEventResultMapper {

  @Mapping(target = "originalEventTs", source = "event.ts")
  @Mapping(target = "jobExecutionId", source = "event.jobExecutionId")
  @Mapping(target = "endDate", expression = "java(java.time.OffsetDateTime.now())")
  @Mapping(target = "resourcesCount", expression = "java(importReport.getImports().size())")
  @Mapping(target = "createdCount",
    expression = "java(importReport.getIdsWithStatus(ImportUtils.Status.CREATED).size())"
  )
  @Mapping(target = "updatedCount",
    expression = "java(importReport.getIdsWithStatus(ImportUtils.Status.UPDATED).size())"
  )
  @Mapping(target = "failedResources",
    expression = "java(getFailedResources(importReport.getImports()))"
  )
  public abstract ImportResultEvent fromImportReport(ImportOutputEvent event, OffsetDateTime startDate,
                                                     ImportReport importReport);

  protected Set<FailedResource> getFailedResources(List<ImportedResource> imports) {
    return imports.stream()
      .filter(ir -> ir.getStatus() == FAILED)
      .map(ir ->
        new FailedResource()
          .description(ir.getFailureReason())
          .lineNumber(ir.getLineNumber())
      )
      .collect(Collectors.toCollection(LinkedHashSet::new));
  }

}
