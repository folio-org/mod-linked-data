package org.folio.linked.data.mapper.kafka.ldimport;

import static java.util.Objects.isNull;
import static org.folio.linked.data.util.ImportUtils.ImportReport;
import static org.folio.linked.data.util.ImportUtils.ImportedResource;
import static org.folio.linked.data.util.ImportUtils.Status;
import static org.folio.linked.data.util.ImportUtils.Status.FAILED;
import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.beans.factory.annotation.Autowired;

@Log4j2
@Mapper(componentModel = SPRING, imports = Status.class)
public abstract class ImportEventResultMapper {

  @SuppressWarnings("java:S6813")
  @Autowired
  protected ObjectMapper objectMapper;

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
          .resource(serializeResource(ir.getFailedResource()))
      )
      .collect(Collectors.toCollection(LinkedHashSet::new));
  }

  private String serializeResource(org.folio.ld.dictionary.model.Resource resource) {
    if (isNull(resource)) {
      return null;
    }
    try {
      return objectMapper.writeValueAsString(resource);
    } catch (JsonProcessingException e) {
      log.warn("Failed to serialize resource: {}", resource.getId(), e);
      return null;
    }
  }
}
