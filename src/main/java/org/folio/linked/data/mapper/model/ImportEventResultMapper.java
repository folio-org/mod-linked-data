package org.folio.linked.data.mapper.model;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.folio.linked.data.model.entity.imprt.ImportEventFailedResource;
import org.folio.linked.data.model.entity.imprt.ImportEventResult;
import org.folio.linked.data.util.ImportUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = SPRING, imports = ImportUtils.Status.class)
public abstract class ImportEventResultMapper {

  @Autowired
  private ObjectMapper objectMapper;

  @Mapping(target = "resourcesCount", expression = "java(importReport.getImports().size())")
  @Mapping(target = "createdCount",
    expression = "java(importReport.getIdsWithStatus(ImportUtils.Status.CREATED).size())"
  )
  @Mapping(target = "updatedCount",
    expression = "java(importReport.getIdsWithStatus(ImportUtils.Status.UPDATED).size())"
  )
  @Mapping(target = "failedCount",
    expression = "java(importReport.getIdsWithStatus(ImportUtils.Status.FAILED).size())"
  )
  @Mapping(target = "failedResources",
    expression = "java(getFailedResources(importEventResult, importReport.getImports()))"
  )
  public abstract ImportEventResult fromImportReport(String eventTs, Long jobId, ImportUtils.ImportReport importReport);

  protected Set<ImportEventFailedResource> getFailedResources(ImportEventResult importEventResult,
                                                              List<ImportUtils.ImportedResource> imports) {
    return imports.stream()
      .map(ImportUtils.ImportedResource::getFailedResource)
      .filter(Objects::nonNull)
      .map(ir -> {
        try {
          return objectMapper.writeValueAsString(ir);
        } catch (JsonProcessingException e) {
          return "mapping to json failed: " + e.getMessage();
        }
      })
      .map(s -> new ImportEventFailedResource()
        .setImportEventResult(importEventResult)
        .setRawResource(s)
      )
      .collect(Collectors.toCollection(LinkedHashSet::new));
  }
}
