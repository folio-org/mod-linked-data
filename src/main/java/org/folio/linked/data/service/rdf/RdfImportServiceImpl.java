package org.folio.linked.data.service.rdf;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.StringUtils.substringAfterLast;
import static org.apache.commons.lang3.StringUtils.substringBefore;
import static org.folio.ld.dictionary.PropertyDictionary.LINK;
import static org.folio.linked.data.util.ImportUtils.APPLICATION_LD_JSON_VALUE;
import static org.folio.linked.data.util.ImportUtils.ImportReport;
import static org.folio.linked.data.util.ImportUtils.ImportedResource;
import static org.folio.linked.data.util.ImportUtils.Status;
import static org.folio.linked.data.util.ImportUtils.Status.CONVERTED;
import static org.folio.linked.data.util.ImportUtils.Status.CREATED;
import static org.folio.linked.data.util.ImportUtils.Status.FAILED;
import static org.folio.linked.data.util.ImportUtils.Status.UPDATED;
import static org.folio.linked.data.util.ImportUtils.toRdfMediaType;
import static org.folio.linked.data.util.ResourceUtils.getPropertyValues;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.domain.dto.ImportFileResponseDto;
import org.folio.linked.data.domain.dto.ImportOutputEvent;
import org.folio.linked.data.domain.dto.ImportResultEvent;
import org.folio.linked.data.domain.dto.ResourceWithLineNumber;
import org.folio.linked.data.exception.RequestProcessingExceptionBuilder;
import org.folio.linked.data.integration.http.HttpClient;
import org.folio.linked.data.mapper.ResourceModelMapper;
import org.folio.linked.data.mapper.kafka.ldimport.ImportEventResultMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.service.lccn.LccnResourceService;
import org.folio.linked.data.service.resource.graph.ResourceGraphService;
import org.folio.linked.data.service.resource.meta.MetadataService;
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

  private final HttpClient httpClient;
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
      var importReport = importInputStream(is, toRdfMediaType(multipartFile.getContentType()), true);
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
    var report = doImport(event.getResourcesWithLineNumbers(), true);
    var importEventResult = importEventResultMapper.fromImportReport(event, startTime, report);
    importResultEventProducer.sendMessages(List.of(importEventResult));
  }

  @Override
  public Resource importRdfUrl(String rdfUrl, Boolean save) {
    var rdfJson = httpClient.downloadString(rdfUrl);
    var imported = importRdfJsonString(rdfJson, save);
    var id = substringBefore(substringAfterLast(rdfUrl, "/"), ".");
    return imported.stream()
      .filter(r -> getPropertyValues(r, LINK).stream().anyMatch(p -> p.contains(id)))
      .findFirst()
      .orElseThrow(() -> exceptionBuilder.notFoundRdfByUriException(rdfUrl));
  }

  private Set<Resource> importRdfJsonString(String rdfJson, Boolean save) {
    try (var inputStream = new ByteArrayInputStream(rdfJson.getBytes(UTF_8))) {
      var importReport = importInputStream(inputStream, APPLICATION_LD_JSON_VALUE, save);
      return importReport.getImports().stream()
        .map(ImportedResource::getResourceEntity)
        .filter(Objects::nonNull)
        .collect(toSet());
    } catch (IOException e) {
      throw exceptionBuilder.badRequestException("Rdf JSON import error", e.getMessage());
    }
  }

  private ImportReport importInputStream(InputStream input, String contentType, Boolean save) {
    var resources = rdf4LdService.mapBibframe2RdfToLd(input, contentType);
    var lineNumber = new AtomicLong(1);
    var resourcesWithLineNumbers = resources.stream()
      .map(r -> new ResourceWithLineNumber(lineNumber.getAndIncrement(), r))
      .collect(toSet());
    return doImport(resourcesWithLineNumbers, save);
  }

  private ImportReport doImport(Set<ResourceWithLineNumber> resourcesWithLineNumbers, boolean save) {
    var report = new ImportReport();
    var mockResourcesSearchResult = lccnResourceService.findMockResources(
      resourcesWithLineNumbers.stream().map(ResourceWithLineNumber::getResource).collect(toSet())
    );
    resourcesWithLineNumbers.forEach(resourceWithLineNumber -> {
      try {
        var resourceModel = lccnResourceService.unMockLccnEdges(resourceWithLineNumber.getResource(),
          mockResourcesSearchResult);
        var resource = resourceModelMapper.toEntity(resourceModel);
        metadataService.ensure(resource);
        Status status;
        if (save) {
          var saveGraphResult = resourceGraphService.saveMergingGraphInNewTransaction(resource);
          status = saveGraphResult.newResources().contains(resource) ? CREATED : UPDATED;
        } else {
          status = CONVERTED;
        }
        report.addImport(new ImportedResource(resourceWithLineNumber, status, null, resource));
      } catch (Exception e) {
        log.debug("Exception during import of a resource from line {}", resourceWithLineNumber.getLineNumber(), e);
        report.addImport(new ImportedResource(resourceWithLineNumber, FAILED, e.getMessage(), null));
      }
    });
    return report;
  }

}
