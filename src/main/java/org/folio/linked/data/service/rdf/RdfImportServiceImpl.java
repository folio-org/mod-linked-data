package org.folio.linked.data.service.rdf;

import static java.util.stream.Collectors.toSet;
import static org.folio.linked.data.util.ImportUtils.Status.CREATED;
import static org.folio.linked.data.util.ImportUtils.Status.FAILED;
import static org.folio.linked.data.util.ImportUtils.Status.UPDATED;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.domain.dto.ImportFileResponseDto;
import org.folio.linked.data.domain.dto.ImportOutputEvent;
import org.folio.linked.data.domain.dto.ImportResultEvent;
import org.folio.linked.data.domain.dto.ResourceWithLineNumber;
import org.folio.linked.data.exception.RequestProcessingExceptionBuilder;
import org.folio.linked.data.mapper.ResourceModelMapper;
import org.folio.linked.data.mapper.kafka.ldimport.ImportEventResultMapper;
import org.folio.linked.data.service.lccn.LccnResourceService;
import org.folio.linked.data.service.resource.graph.ResourceGraphService;
import org.folio.linked.data.service.resource.meta.MetadataService;
import org.folio.linked.data.util.ImportUtils;
import org.folio.rdf4ld.service.Rdf4LdService;
import org.folio.spring.tools.kafka.FolioMessageProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Log4j2
@Component
@Transactional
@RequiredArgsConstructor
public class RdfImportServiceImpl implements RdfImportService {

  private final Rdf4LdService rdf4LdService;
  private final MetadataService metadataService;
  private final LccnResourceService lccnResourceService;
  private final ResourceModelMapper resourceModelMapper;
  private final ResourceGraphService resourceGraphService;
  private final ImportEventResultMapper importEventResultMapper;
  private final RequestProcessingExceptionBuilder exceptionBuilder;
  @SuppressWarnings("java:S6813") // Autowired, because isn't available for the standalone profile
  @Autowired(required = false)
  private FolioMessageProducer<ImportResultEvent> importResultEventProducer;

  @Override
  public ImportFileResponseDto importFile(MultipartFile multipartFile) {
    try (var is = multipartFile.getInputStream()) {
      var resources = rdf4LdService.mapBibframe2RdfToLd(is, ImportUtils.toRdfMediaType(multipartFile.getContentType()));
      var resourcesWithLineNumbers = resources.stream().map(r -> new ResourceWithLineNumber(1L, r)).collect(toSet());
      var importReport = doImport(resourcesWithLineNumbers);
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
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void importOutputEvent(ImportOutputEvent event, OffsetDateTime startTime) {
    var report = doImport(event.getResourcesWithLineNumbers());
    var importEventResult = importEventResultMapper.fromImportReport(event, startTime, report);
    importResultEventProducer.sendMessages(List.of(importEventResult));
  }

  private ImportUtils.ImportReport doImport(Set<ResourceWithLineNumber> resourcesWithLineNumbers) {
    var report = new ImportUtils.ImportReport();
    var mockResourcesSearchResult = lccnResourceService.findMockResources(
      resourcesWithLineNumbers.stream().map(ResourceWithLineNumber::getResource).collect(toSet())
    );
    resourcesWithLineNumbers.forEach(resourceWithLineNumber -> {
      try {
        var resourceModel = lccnResourceService.unMockLccnEdges(resourceWithLineNumber.getResource(),
          mockResourcesSearchResult);
        var resource = resourceModelMapper.toEntity(resourceModel);
        metadataService.ensure(resource);
        var saveGraphResult = resourceGraphService.saveMergingGraphInNewTransaction(resource);
        var status = saveGraphResult.newResources().contains(resource) ? CREATED : UPDATED;
        report.addImport(new ImportUtils.ImportedResource(resourceWithLineNumber, status, null));
      } catch (Exception e) {
        log.debug("Exception during import of a resource from line {}", resourceWithLineNumber.getLineNumber(), e);
        report.addImport(new ImportUtils.ImportedResource(resourceWithLineNumber, FAILED, e.getMessage()));
      }
    });
    return report;
  }

}
