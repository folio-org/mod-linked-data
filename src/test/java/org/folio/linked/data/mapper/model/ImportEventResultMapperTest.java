package org.folio.linked.data.mapper.model;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.folio.linked.data.util.ImportUtils.Status.CREATED;
import static org.folio.linked.data.util.ImportUtils.Status.FAILED;
import static org.folio.linked.data.util.ImportUtils.Status.UPDATED;

import java.time.OffsetDateTime;
import org.folio.ld.dictionary.model.Resource;
import org.folio.linked.data.domain.dto.ImportOutputEvent;
import org.folio.linked.data.domain.dto.ResourceWithLineNumber;
import org.folio.linked.data.mapper.kafka.ldimport.ImportEventResultMapperImpl;
import org.folio.linked.data.util.ImportUtils;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class ImportEventResultMapperTest {

  @InjectMocks
  private ImportEventResultMapperImpl mapper;

  @Test
  void fromImportReport_shouldFullyMapGivenImportReport() {
    // given
    var ir = new ImportUtils.ImportReport();
    ir.addImport(getImportedResource(1L, "created", 1L, CREATED, null));
    ir.addImport(getImportedResource(2L, "updated", 2L, UPDATED, null));
    var failureReason = "failure reason";
    ir.addImport(getImportedResource(3L, "failed", 3L, FAILED, failureReason));
    var eventTs = "12345";
    var jobId = 777L;
    var event = new ImportOutputEvent().ts(eventTs).jobExecutionId(jobId);
    var startDate = OffsetDateTime.now().minusMinutes(1);

    // when
    var result = mapper.fromImportReport(event, startDate, ir);

    // then
    assertThat(result.getOriginalEventTs()).isEqualTo(eventTs);
    assertThat(result.getJobExecutionId()).isEqualTo(jobId);
    assertThat(result.getStartDate()).isEqualTo(startDate);
    assertThat(result.getEndDate().isAfter(result.getStartDate())).isTrue();
    assertThat(result.getResourcesCount()).isEqualTo(3);
    assertThat(result.getCreatedCount()).isEqualTo(1);
    assertThat(result.getUpdatedCount()).isEqualTo(1);
    assertThat(result.getFailedResources()).hasSize(1);
    var failedResource = result.getFailedResources().iterator().next();
    assertThat(failedResource.getLineNumber()).isEqualTo(3L);
    assertThat(failedResource.getDescription()).isEqualTo(failureReason);
  }

  @Test
  void fromImportReport_shouldNotFail_ifGivenImportReportIsEmpty() {
    // given
    var eventTs = "12345";
    var jobId = 777L;
    var event = new ImportOutputEvent().ts(eventTs).jobExecutionId(jobId);
    var ir = new ImportUtils.ImportReport();

    // when
    var result = mapper.fromImportReport(event, OffsetDateTime.now(), ir);

    // then
    assertThat(result.getOriginalEventTs()).isEqualTo(eventTs);
    assertThat(result.getJobExecutionId()).isEqualTo(jobId);
    assertThat(result.getResourcesCount()).isZero();
    assertThat(result.getCreatedCount()).isZero();
    assertThat(result.getUpdatedCount()).isZero();
    assertThat(result.getFailedResources()).isEmpty();
  }

  private ImportUtils.ImportedResource getImportedResource(Long id,
                                                           String label,
                                                           Long lineNumber,
                                                           ImportUtils.Status status,
                                                           String failureReason) {
    var resource = new Resource().setId(id).setLabel(label);
    var resourceWithLineNumber = new ResourceWithLineNumber(lineNumber, resource);
    return new ImportUtils.ImportedResource(resourceWithLineNumber, status, failureReason, null);
  }
}
