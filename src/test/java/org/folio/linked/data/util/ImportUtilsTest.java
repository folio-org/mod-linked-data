package org.folio.linked.data.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.linked.data.util.ImportUtils.Status.CREATED;
import static org.folio.linked.data.util.ImportUtils.Status.FAILED;
import static org.folio.linked.data.util.ImportUtils.Status.UPDATED;

import java.util.stream.Stream;
import org.folio.ld.dictionary.model.Resource;
import org.folio.linked.data.domain.dto.ResourceWithLineNumber;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

@UnitTest
class ImportUtilsTest {
  private static Stream<Arguments> resources() {
    return Stream.of(
      Arguments.of(
        new ImportUtils.ImportedResource(
          new ResourceWithLineNumber(1L, new Resource().setId(1L).setLabel("")), CREATED, null, null),
        "ID,LABEL,STATUS,FAILURE_REASON\r\n1,,Created,\r\n"
      ),
      Arguments.of(
        new ImportUtils.ImportedResource(new ResourceWithLineNumber(2L,
          new Resource().setId(692094L).setLabel("Some complexity, isn't \"missing\"")), UPDATED, null, null),
        "ID,LABEL,STATUS,FAILURE_REASON\r\n692094,\"Some complexity, isn't \"\"missing\"\"\",Updated,\r\n"
      ),
      Arguments.of(
        new ImportUtils.ImportedResource(new ResourceWithLineNumber(3L,
          new Resource().setId(5L).setLabel("No good")), FAILED, "Some, error", null),
        "ID,LABEL,STATUS,FAILURE_REASON\r\n5,No good,Failed,\"Some, error\"\r\n"
      )
    );
  }

  @ParameterizedTest
  @CsvSource(value = {
    "application/json,application/ld+json",
    "application/ld+json,application/ld+json",
    "text/plain,text/turtle",
    "application/xml,application/xml",
    "application/rdf+xml,application/rdf+xml",
    "image/png,image/png",
    "'',''",
    "null,null"
  }, nullValues = {"null"})
  void toRdfMediaType_shouldReturnUpdatedMediaType(String original, String expected) {
    assertThat(ImportUtils.toRdfMediaType(original)).isEqualTo(expected);
  }

  @ParameterizedTest
  @MethodSource("resources")
  void importReportToCsv_shouldGenerateCsv(ImportUtils.ImportedResource res, String expected) {
    var report = new ImportUtils.ImportReport();
    report.addImport(res);
    assertThat(report.toCsv()).isEqualTo(expected);
  }
}
