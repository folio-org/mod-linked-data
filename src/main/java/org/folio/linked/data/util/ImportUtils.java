package org.folio.linked.data.util;

import static org.folio.linked.data.util.ImportUtils.Status.FAILED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.folio.ld.dictionary.model.Resource;
import org.folio.linked.data.domain.dto.ResourceWithLineNumber;

@Log4j2
@UtilityClass
public class ImportUtils {
  public static final String APPLICATION_LD_JSON_VALUE = "application/ld+json";
  public static final String TEXT_TURTLE_VALUE = "text/turtle";

  /**
   * Content type is guessed by the browser File API implementation / the
   * underlying OS and probably doesn't correspond to MIME types the rdf4j
   * library understands. Map common generic MIME types the browser might
   * guess to the appropriate rdf4j equivalent. rdf4j accepts application/xml
   * already, no map required.
   *
   * @see <a href="https://rdf4j.org/javadoc/latest/org/eclipse/rdf4j/rio/RDFFormat.html">rdf4j documentation</a>
   */
  public static String toRdfMediaType(String contentType) {
    if (contentType == null) {
      return null;
    }
    return switch (contentType) {
      case APPLICATION_JSON_VALUE -> APPLICATION_LD_JSON_VALUE;
      case TEXT_PLAIN_VALUE -> TEXT_TURTLE_VALUE; // Could also be text/n3, assuming ttl is more prevalent
      default -> contentType;
    };
  }

  private enum ReportHeader {
    ID,
    LABEL,
    STATUS,
    FAILURE_REASON;
  }

  @RequiredArgsConstructor
  public enum Status {
    CREATED("Created"),
    UPDATED("Updated"),
    FAILED("Failed");

    private final String value;
  }

  @Data
  public static class ImportedResource {
    private Long id;
    private String label;
    private Long lineNumber;
    private Status status;
    private String failureReason;
    private Resource failedResource;

    public ImportedResource(ResourceWithLineNumber resourceWithLineNumber, Status status, String failureReason) {
      this.id = resourceWithLineNumber.getResource().getId();
      this.label = resourceWithLineNumber.getResource().getLabel();
      this.lineNumber = resourceWithLineNumber.getLineNumber();
      this.status = status;
      this.failureReason = failureReason;
      this.failedResource = status == FAILED ? resourceWithLineNumber.getResource() : null;
    }
  }

  @Data
  public static class ImportReport {
    private List<ImportedResource> imports = new ArrayList<>();

    public void addImport(ImportedResource resource) {
      this.imports.add(resource);
    }

    public String toCsv() {
      var sw = new StringWriter();
      var format = CSVFormat.EXCEL.builder()
        .setHeader(ReportHeader.class)
        .get();
      try (var printer = new CSVPrinter(sw, format)) {
        for (var resource : imports) {
          printer.printRecord(
            resource.getId(),
            resource.getLabel(),
            resource.getStatus().value,
            resource.getFailureReason()
          );
        }
      } catch (IOException e) {
        log.warn("I/O error while generating CSV report", e);
      }
      return sw.toString();
    }

    public List<String> getIdsWithStatus(Status... statuses) {
      return imports.stream()
        .filter(ir -> Arrays.stream(statuses).toList().contains(ir.status))
        .map(ImportedResource::getId)
        .map(Object::toString)
        .toList();
    }
  }
}
