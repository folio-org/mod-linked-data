package org.folio.linked.data.util;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.folio.ld.dictionary.ResourceTypeDictionary.HUB;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.linked.data.util.ResourceUtils.getTypeName;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;
import static org.springframework.util.CollectionUtils.isEmpty;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.domain.dto.ResourceWithLineNumber;
import org.folio.linked.data.model.entity.Resource;
import org.jspecify.annotations.NonNull;

@Log4j2
@UtilityClass
public class ImportUtil {
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
    TYPE,
    LABEL,
    STATUS,
    FAILURE_REASON;
  }

  @RequiredArgsConstructor
  public enum Status {
    CREATED("Created"),
    UPDATED("Updated"),
    CONVERTED("Converted"),
    FAILED("Failed");

    private final String value;
  }

  @Data
  public static class ImportedResource {
    private Long id;
    private Set<ResourceTypeDictionary> types;
    private String label;
    private Long lineNumber;
    private Status status;
    private String failureReason;
    private Resource resourceEntity;

    public ImportedResource(ResourceWithLineNumber resourceWithLineNumber,
                            Status status,
                            String failureReason,
                            Resource resourceEntity) {
      this.id = resourceWithLineNumber.getResource().getId();
      this.types = resourceWithLineNumber.getResource().getTypes();
      this.label = resourceWithLineNumber.getResource().getLabel();
      this.lineNumber = resourceWithLineNumber.getLineNumber();
      this.status = status;
      this.failureReason = failureReason;
      this.resourceEntity = resourceEntity;
    }
  }

  @Data
  public static class ImportReport {
    private static final CSVFormat FORMAT = CSVFormat.EXCEL.builder()
      .setHeader(ReportHeader.class)
      .setDelimiter(';')
      .setRecordSeparator("\n")
      .get();
    private List<ImportedResource> imports = new ArrayList<>();

    public void addImport(ImportedResource resource) {
      this.imports.add(resource);
    }

    public String toCsv() {
      var sw = new StringWriter();
      try (var printer = new CSVPrinter(sw, FORMAT)) {
        for (var resource : imports) {
          printer.printRecord(
            resource.getId(),
            printType(resource),
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

    private @NonNull String printType(ImportedResource ir) {
      if (isEmpty(ir.getTypes())) {
        return EMPTY;
      }
      if (ir.getTypes().contains(INSTANCE)) {
        return getTypeName(INSTANCE);
      }
      if (ir.getTypes().contains(HUB)) {
        return getTypeName(HUB);
      }
      return EMPTY;
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
