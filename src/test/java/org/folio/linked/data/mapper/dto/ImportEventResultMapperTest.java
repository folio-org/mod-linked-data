package org.folio.linked.data.mapper.dto;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.folio.linked.data.util.ImportUtils.Status.CREATED;
import static org.folio.linked.data.util.ImportUtils.Status.FAILED;
import static org.folio.linked.data.util.ImportUtils.Status.UPDATED;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.folio.ld.dictionary.model.Resource;
import org.folio.linked.data.util.ImportUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ImportEventResultMapperTest {

  @InjectMocks
  private ImportEventResultMapperImpl mapper;
  @Mock
  private ObjectMapper objectMapper;

  @Test
  void shouldFullyMapGivenImportReport() throws JsonProcessingException {
    // given
    var ir = new ImportUtils.ImportReport();
    ir.addImport(getImportedResource(1L, "created", CREATED, null));
    ir.addImport(getImportedResource(2L, "updated", UPDATED, null));
    ir.addImport(getImportedResource(3L, "failed", FAILED, "failure reason"));
    var eventTs = "12345";
    var jobId = 777L;

    // when
    var result = mapper.fromImportReport(eventTs, jobId, ir);

    // then
    assertThat(result.getOutputEventTs()).isEqualTo(eventTs);
    assertThat(result.getJobInstanceId()).isEqualTo(jobId);
    assertThat(result.getResourcesCount()).isEqualTo(3);
    assertThat(result.getCreatedCount()).isEqualTo(1);
    assertThat(result.getUpdatedCount()).isEqualTo(1);
    assertThat(result.getFailedCount()).isEqualTo(1);
    assertThat(result.getFailedResources()).hasSize(1);
    var importEventFailedResource = result.getFailedResources().iterator().next();
    assertThat(importEventFailedResource).isEqualTo(new Resource().setId(3L).setLabel("failed"));
  }

  @Test
  void shouldNotFail_ifGivenImportReportIsEmpty() {
    // given
    var eventTs = "12345";
    var jobId = 777L;
    var ir = new ImportUtils.ImportReport();

    // when
    var result = mapper.fromImportReport(eventTs, jobId, ir);

    // then
    assertThat(result.getOutputEventTs()).isEqualTo(Long.parseLong(eventTs));
    assertThat(result.getJobInstanceId()).isEqualTo(jobId);
    assertThat(result.getResourcesCount()).isZero();
    assertThat(result.getCreatedCount()).isZero();
    assertThat(result.getUpdatedCount()).isZero();
    assertThat(result.getFailedCount()).isZero();
    assertThat(result.getFailedResources()).isEmpty();
  }

  @Test
  void shouldFullyMapGivenImportReport_whenFailedResourceNotCorrectJson() throws JsonProcessingException {
    // given
    var ir = new ImportUtils.ImportReport();
    ir.addImport(getImportedResource(1L, "failed", FAILED, "failure reason"));
    var eventTs = "12345";
    var jobId = 777L;

    // when
    var result = mapper.fromImportReport(eventTs, jobId, ir);

    // then
    assertThat(result.getOutputEventTs()).isEqualTo(Long.parseLong(eventTs));
    assertThat(result.getJobInstanceId()).isEqualTo(jobId);
    assertThat(result.getResourcesCount()).isEqualTo(1);
    assertThat(result.getCreatedCount()).isZero();
    assertThat(result.getUpdatedCount()).isZero();
    assertThat(result.getFailedCount()).isEqualTo(1);
    assertThat(result.getFailedResources()).hasSize(1);
    var importEventFailedResource = result.getFailedResources().iterator().next();
    assertThat(importEventFailedResource).isEqualTo(new Resource().setId(1L).setLabel("failed"));
  }

  private ImportUtils.ImportedResource getImportedResource(Long id, String label,
                                                           ImportUtils.Status status, String failureReason) {
    return new ImportUtils.ImportedResource(new Resource().setId(id).setLabel(label), status, failureReason);
  }
}
