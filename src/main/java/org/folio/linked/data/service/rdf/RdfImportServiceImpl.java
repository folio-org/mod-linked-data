package org.folio.linked.data.service.rdf;

import static org.folio.linked.data.util.ImportUtils.Status.CREATED;
import static org.folio.linked.data.util.ImportUtils.Status.FAILED;
import static org.folio.linked.data.util.ImportUtils.Status.UPDATED;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.ld.dictionary.model.Resource;
import org.folio.linked.data.domain.dto.ImportFileResponseDto;
import org.folio.linked.data.domain.dto.ImportOutputEvent;
import org.folio.linked.data.exception.RequestProcessingExceptionBuilder;
import org.folio.linked.data.mapper.ResourceModelMapper;
import org.folio.linked.data.mapper.model.ImportEventResultMapper;
import org.folio.linked.data.repo.ImportEventResultRepository;
import org.folio.linked.data.service.resource.events.ResourceEventsPublisher;
import org.folio.linked.data.service.resource.graph.ResourceGraphService;
import org.folio.linked.data.service.resource.meta.MetadataService;
import org.folio.linked.data.util.ImportUtils;
import org.folio.rdf4ld.service.Rdf4LdService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Log4j2
@Component
@Transactional
@RequiredArgsConstructor
public class RdfImportServiceImpl implements RdfImportService {

  private final Rdf4LdService rdf4LdService;
  private final MetadataService metadataService;
  private final ResourceModelMapper resourceModelMapper;
  private final ResourceGraphService resourceGraphService;
  private final ImportEventResultMapper importEventResultMapper;
  private final ResourceEventsPublisher resourceEventsPublisher;
  private final RequestProcessingExceptionBuilder exceptionBuilder;
  private final ImportEventResultRepository importEventResultRepository;

  @Override
  public ImportFileResponseDto importFile(MultipartFile multipartFile) {
    try (var is = multipartFile.getInputStream()) {
      var resources = rdf4LdService.mapBibframe2RdfToLd(is, ImportUtils.toRdfMediaType(multipartFile.getContentType()));
      var importReport = doImport(resources);
      var reportCsv = importReport.toCsv();
      return new ImportFileResponseDto(importReport.getIdsWithStatus(CREATED, UPDATED), reportCsv);
    } catch (IOException e) {
      throw exceptionBuilder.badRequestException("Rdf import incoming file reading error", e.getMessage());
    } catch (Exception e) {
      log.error("Rdf import error", e);
      return new ImportFileResponseDto(List.of(), e.getMessage());
    }
  }

  @Override
  public void importOutputEvent(ImportOutputEvent event) {
    var report = doImport(event.getResources());
    var importEventResult = importEventResultMapper.fromImportReport(event.getTs(), event.getJobInstanceId(), report);
    importEventResultRepository.save(importEventResult);
  }

  private ImportUtils.ImportReport doImport(Collection<Resource> resources) {
    var report = new ImportUtils.ImportReport();
    resources.forEach(resourceModel -> {
      try {
        var resource = resourceModelMapper.toEntity(resourceModel);
        metadataService.ensure(resource);
        var saveGraphResult = resourceGraphService.saveMergingGraph(resource);
        resourceEventsPublisher.emitEventsForCreateAndUpdate(saveGraphResult, null);
        var status = saveGraphResult.newResources().contains(resource) ? CREATED : UPDATED;
        report.addImport(new ImportUtils.ImportedResource(resourceModel, status, null));
      } catch (Exception e) {
        log.debug("Exception during import of a resource with ID {}", resourceModel.getId(), e);
        report.addImport(new ImportUtils.ImportedResource(resourceModel, FAILED, e.getMessage()));
      }
    });
    return report;
  }

}
