package org.folio.linked.data.service.rdf;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.domain.dto.ImportFileResponseDto;
import org.folio.linked.data.exception.RequestProcessingExceptionBuilder;
import org.folio.linked.data.mapper.ResourceModelMapper;
import org.folio.linked.data.model.entity.event.ResourceCreatedEvent;
import org.folio.linked.data.repo.ResourceRepository;
import org.folio.linked.data.service.resource.graph.ResourceGraphService;
import org.folio.linked.data.service.resource.meta.MetadataService;
import org.folio.linked.data.util.ImportUtils;
import org.folio.rdf4ld.service.Rdf4LdService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Log4j2
@Component
@RequiredArgsConstructor
public class RdfImportServiceImpl implements RdfImportService {

  private final Rdf4LdService rdf4LdService;
  private final MetadataService metadataService;
  private final ResourceRepository resourceRepo;
  private final ResourceModelMapper resourceModelMapper;
  private final ResourceGraphService resourceGraphService;
  private final RequestProcessingExceptionBuilder exceptionBuilder;
  private final ApplicationEventPublisher applicationEventPublisher;

  @Override
  public ImportFileResponseDto importFile(MultipartFile multipartFile) {
    try (var is = multipartFile.getInputStream()) {
      var resources = rdf4LdService.mapToLdInstance(is, ImportUtils.toRdfMediaType(multipartFile.getContentType()));
      return save(resources);
    } catch (IOException e) {
      throw exceptionBuilder.badRequestException("Rdf import incoming file reading error", e.getMessage());
    }
  }

  private ImportFileResponseDto save(Set<org.folio.ld.dictionary.model.Resource> resources) {
    ImportFileResponseDto response;
    String reportCsv = "";
    ImportUtils.ImportReport report = new ImportUtils.ImportReport();
    List<Long> ids = resources.stream()
      .map(resourceModelMapper::toEntity)
      .filter(r -> {
        boolean exists = resourceRepo.existsById(r.getId());
        if (exists) {
          report.addImport(
            new ImportUtils.ImportedResource(
              r.getId(),
              r.getLabel(),
              ImportUtils.Status.FAILURE,
              "Already exists in graph"));
        }
        return !exists;
      })
      .map(resource -> {
        metadataService.ensure(resource);
        var saved = resourceGraphService.saveMergingGraph(resource);
        applicationEventPublisher.publishEvent(new ResourceCreatedEvent(saved));
        report.addImport(
          new ImportUtils.ImportedResource(
            saved.getId(),
            saved.getLabel(),
            ImportUtils.Status.SUCCESS,
            ""));
        return resource.getId();
      })
      .toList();
    try {
      reportCsv = report.toCsv();
    } catch (IOException e) {
      log.warn("I/O error while generating CSV report, returning empty report: {}", e);
    }
    response = new ImportFileResponseDto(ids, reportCsv);
    return response;
  }
}
