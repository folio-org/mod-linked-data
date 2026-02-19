package org.folio.linked.data.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.ResourceTypeDictionary.HUB;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.linked.data.util.ImportUtil.Status.CREATED;
import static org.folio.linked.data.util.ImportUtil.Status.FAILED;
import static org.folio.linked.data.util.ImportUtil.Status.UPDATED;

import java.util.stream.Stream;
import org.folio.ld.dictionary.model.Resource;
import org.folio.linked.data.domain.dto.ResourceWithLineNumber;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

@UnitTest
class ImportUtilTest {

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
    assertThat(ImportUtil.toRdfMediaType(original)).isEqualTo(expected);
  }

  @ParameterizedTest
  @MethodSource("resources")
  void importReportToCsv_shouldGenerateCsv(ImportUtil.ImportedResource res, String expected) {
    var report = new ImportUtil.ImportReport();
    report.addImport(res);
    assertThat(report.toCsv()).isEqualTo(expected);
  }

  private static Stream<Arguments> resources() {
    return Stream.of(
      Arguments.of(
        new ImportUtil.ImportedResource(
          new ResourceWithLineNumber(1L, new Resource().setId(1L).setLabel("").addType(INSTANCE)), CREATED,
          null, null), "ID;TYPE;LABEL;STATUS;FAILURE_REASON\n1;Instance;;Created;\n"
      ),
      Arguments.of(
        new ImportUtil.ImportedResource(new ResourceWithLineNumber(2L,
          new Resource().setId(692094L).setLabel("Some complexity, isn't \"missing\"").addType(HUB)), UPDATED, null,
          null),
        "ID;TYPE;LABEL;STATUS;FAILURE_REASON\n692094;Hub;\"Some complexity, isn't \"\"missing\"\"\";Updated;\n"
      ),
      Arguments.of(
        new ImportUtil.ImportedResource(new ResourceWithLineNumber(3L,
          new Resource().setId(5L).setLabel("No good")), FAILED, "Some, error", null),
        "ID;TYPE;LABEL;STATUS;FAILURE_REASON\n5;;No good;Failed;Some, error\n"
      )
    );
  }
}
