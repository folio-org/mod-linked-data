package org.folio.linked.data.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;
import lombok.SneakyThrows;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

@UnitTest
class ImportUtilsTest {
  @ParameterizedTest
  @CsvSource({
    "application/json,application/ld+json",
    "application/ld+json,application/ld+json",
    "text/plain,text/turtle",
    "application/xml,application/xml",
    "application/rdf+xml,application/rdf+xml",
    "image/png,image/png",
    ",",
  })
  void toRdfMediaType_shouldReturnUpdatedMediaType(String original, String expected) {
    assertThat(ImportUtils.toRdfMediaType(original)).isEqualTo(expected);
  }

  private static Stream<Arguments> resources() {
    return Stream.of(
      Arguments.of(
        new ImportUtils.ImportedResource(1L, "", ImportUtils.Status.SUCCESS, ""),
        "ID,LABEL,STATUS,FAILURE_REASON\r\n1,,Success,\r\n"
      ),
      Arguments.of(
        new ImportUtils.ImportedResource(692094L, "Some complexity, isn't \"missing\"", ImportUtils.Status.SUCCESS, ""),
        "ID,LABEL,STATUS,FAILURE_REASON\r\n692094,\"Some complexity, isn't \"\"missing\"\"\",Success,\r\n"
      ),
      Arguments.of(
        new ImportUtils.ImportedResource(5L, "No good", ImportUtils.Status.FAILURE, "Some, error"),
        "ID,LABEL,STATUS,FAILURE_REASON\r\n5,No good,Failure,\"Some, error\"\r\n"
      )
    );
  }

  @ParameterizedTest
  @MethodSource("resources")
  @SneakyThrows
  void importReportToCsv_shouldGenerateCsv(ImportUtils.ImportedResource res, String expected) {
    var report = new ImportUtils.ImportReport();
    report.addImport(res);
    assertThat(report.toCsv()).isEqualTo(expected);
  }
}
