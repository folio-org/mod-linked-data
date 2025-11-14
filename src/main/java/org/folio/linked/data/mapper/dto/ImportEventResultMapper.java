package org.folio.linked.data.mapper.dto;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import java.util.List;
import org.folio.ld.dictionary.model.Resource;
import org.folio.linked.data.domain.dto.ImportEventResult;
import org.folio.linked.data.util.ImportUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = SPRING, imports = ImportUtils.Status.class)
public abstract class ImportEventResultMapper {

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
    expression = "java(getFailedResources(importReport.getImports()))"
  )
  public abstract ImportEventResult fromImportReport(String outputEventTs, Long jobInstanceId,
                                                     ImportUtils.ImportReport importReport);

  protected List<Resource> getFailedResources(List<ImportUtils.ImportedResource> imports) {
    return imports.stream()
      .map(ImportUtils.ImportedResource::getFailedResource)
      .toList();
  }
}
