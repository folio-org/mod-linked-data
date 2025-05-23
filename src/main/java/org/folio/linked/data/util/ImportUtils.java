package org.folio.linked.data.util;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

@Log4j2
@UtilityClass
public class ImportUtils {
  public static final String APPLICATION_LD_JSON_VALUE = "application/ld+json";
  public static final String TEXT_TURTLE_VALUE = "text/turtle";

  private enum ReportHeader {
    URI,
    LABEL,
    STATUS,
    FAILURE_REASON;
  }

  /**
   * Content type is guessed by the browser File API implementation / the
   * underlying OS and probably doesn't correspond to MIME types the rdf4j
   * library understands. Map common generic MIME types the browser might 
   * guess to the appropriate rdf4j equivalent. rdf4j accepts application/xml
   * already, no map required.
   * @see https://rdf4j.org/javadoc/latest/org/eclipse/rdf4j/rio/RDFFormat.html
   */
  public static String toRdfMediaType(String contentType) {
    String finalType = contentType;
    if (contentType != null) {
      switch (contentType) {
        case APPLICATION_JSON_VALUE:
          finalType = APPLICATION_LD_JSON_VALUE;
          break;
        case TEXT_PLAIN_VALUE:
          finalType = TEXT_TURTLE_VALUE; // Could also be text/n3, assuming ttl is more prevalent
          break;
        default: // nothing to change if no match
      }
    }
    return finalType;
  }

  @RequiredArgsConstructor
  public enum Status {
    SUCCESS("Success"),
    FAILURE("Failure");

    private final String value;
  }

  @Data
  @AllArgsConstructor
  public static class ImportedResource {
    private Long id;
    private String label;
    private Status status;
    private String failureReason;
  }

  @Data
  public static class ImportReport {
    private List<ImportedResource> imports = new ArrayList<>();

    public boolean addImport(ImportedResource resource) {
      return this.imports.add(resource);
    }

    public String toCsv() throws IOException {
      StringWriter sw = new StringWriter();
      CSVFormat format = CSVFormat.EXCEL.builder()
        .setHeader(ReportHeader.class)
        .get();
      try (final CSVPrinter printer = new CSVPrinter(sw, format)) {
        for (ImportedResource resource : imports) {
          printer.printRecord(
            resource.getId(),
            resource.getLabel(),
            resource.getStatus().value,
            resource.getFailureReason());
        }
      }
      return sw.toString();
    }
  }
}
