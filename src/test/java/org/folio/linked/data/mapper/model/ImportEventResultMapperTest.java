package org.folio.linked.data.mapper.model;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.folio.linked.data.util.ImportUtils.Status.CREATED;
import static org.folio.linked.data.util.ImportUtils.Status.FAILED;
import static org.folio.linked.data.util.ImportUtils.Status.UPDATED;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import org.folio.ld.dictionary.model.Resource;
import org.folio.linked.data.domain.dto.ImportOutputEvent;
import org.folio.linked.data.model.entity.imprt.ImportEventResult;
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
  void fromImportReport_shouldFullyMapGivenImportReport() throws JsonProcessingException {
    // given
    var ir = new ImportUtils.ImportReport();
    ir.addImport(getImportedResource(1L, "created", CREATED, null));
    ir.addImport(getImportedResource(2L, "updated", UPDATED, null));
    var failureReason = "failure reason";
    ir.addImport(getImportedResource(3L, "failed", FAILED, failureReason));
    var expectedRawFailedResource = "expectedRawFailedResource";
    when(objectMapper.writeValueAsString(any())).thenReturn(expectedRawFailedResource);
    var eventTs = "12345";
    var jobId = 777L;
    var event = new ImportOutputEvent().ts(eventTs).jobInstanceId(jobId);
    var startDate = LocalDateTime.now().minusMinutes(1);

    // when
    var result = mapper.fromImportReport(event, startDate, ir);

    // then
    assertThat(result.getEventTs()).isEqualTo(Long.parseLong(eventTs));
    assertThat(result.getJobId()).isEqualTo(jobId);
    assertThat(result.getStartDate()).isEqualTo(Timestamp.valueOf(startDate));
    assertThat(result.getEndDate().after(result.getStartDate())).isTrue();
    assertThat(result.getResourcesCount()).isEqualTo(3);
    assertThat(result.getCreatedCount()).isEqualTo(1);
    assertThat(result.getUpdatedCount()).isEqualTo(1);
    assertThat(result.getFailedCount()).isEqualTo(1);
    assertThat(result.getFailedResources()).hasSize(1);
    var importEventFailedResource = result.getFailedResources().iterator().next();
    assertThat(importEventFailedResource.getImportEventResult()).isEqualTo(result);
    assertThat(importEventFailedResource.getRawResource()).isEqualTo(expectedRawFailedResource);
    assertThat(importEventFailedResource.getReason()).isEqualTo(failureReason);
  }

  @Test
  void fromImportReport_shouldNotFail_ifGivenImportReportIsEmpty() {
    // given
    var eventTs = "12345";
    var jobId = 777L;
    var event = new ImportOutputEvent().ts(eventTs).jobInstanceId(jobId);
    var ir = new ImportUtils.ImportReport();

    // when
    var result = mapper.fromImportReport(event, LocalDateTime.now(), ir);

    // then
    assertThat(result.getEventTs()).isEqualTo(Long.parseLong(eventTs));
    assertThat(result.getJobId()).isEqualTo(jobId);
    assertThat(result.getResourcesCount()).isZero();
    assertThat(result.getCreatedCount()).isZero();
    assertThat(result.getUpdatedCount()).isZero();
    assertThat(result.getFailedCount()).isZero();
    assertThat(result.getFailedResources()).isEmpty();
  }

  @Test
  void fromImportReport_shouldFullyMapGivenReport_whenFailedResourceNotCorrectJson() throws JsonProcessingException {
    // given
    var ir = new ImportUtils.ImportReport();
    var failureReason = "failure reason";
    ir.addImport(getImportedResource(1L, "failed", FAILED, failureReason));
    var expectedJsonProcessingException = "expectedJsonProcessingException";
    when(objectMapper.writeValueAsString(any())).thenThrow(new JsonParseException("expectedJsonProcessingException"));
    var eventTs = "12345";
    var jobId = 777L;
    var event = new ImportOutputEvent().ts(eventTs).jobInstanceId(jobId);

    // when
    var result = mapper.fromImportReport(event, LocalDateTime.now(), ir);

    // then
    assertThat(result.getEventTs()).isEqualTo(Long.parseLong(eventTs));
    assertThat(result.getJobId()).isEqualTo(jobId);
    assertThat(result.getResourcesCount()).isEqualTo(1);
    assertThat(result.getCreatedCount()).isZero();
    assertThat(result.getUpdatedCount()).isZero();
    assertThat(result.getFailedCount()).isEqualTo(1);
    assertThat(result.getFailedResources()).hasSize(1);
    var importEventFailedResource = result.getFailedResources().iterator().next();
    assertThat(importEventFailedResource.getImportEventResult()).isEqualTo(result);
    assertThat(importEventFailedResource.getRawResource())
      .isEqualTo("mapping to json failed: " + expectedJsonProcessingException);
    assertThat(importEventFailedResource.getReason()).isEqualTo(failureReason);
  }

  @Test
  void getFailedResources_shouldMapCorrectData() throws JsonProcessingException {
    // given
    var failureReason = "failure reason";
    var imports = List.of(
      getImportedResource(1L, "created", CREATED, null),
      getImportedResource(2L, "updated", UPDATED, null),
      getImportedResource(3L, "failed", FAILED, failureReason)
    );
    var expectedRawFailedResource = "expectedRawFailedResource";
    when(objectMapper.writeValueAsString(any())).thenReturn(expectedRawFailedResource);
    var ier = new ImportEventResult();

    // when
    var result = mapper.getFailedResources(ier, imports);

    // then
    assertThat(result).hasSize(1);
    var importEventFailedResource = result.iterator().next();
    assertThat(importEventFailedResource.getImportEventResult()).isEqualTo(ier);
    assertThat(importEventFailedResource.getRawResource()).isEqualTo(expectedRawFailedResource);
    assertThat(importEventFailedResource.getReason()).isEqualTo(failureReason);
  }

  @Test
  void getFailedResources_shouldMap_whenFailedResourceNotCorrectJson() throws JsonProcessingException {
    // given
    var failureReason = "failure reason";
    var imports = List.of(
      getImportedResource(1L, "created", CREATED, null),
      getImportedResource(2L, "updated", UPDATED, null),
      getImportedResource(3L, "failed", FAILED, failureReason)
    );
    var expectedException = "expectedException";
    when(objectMapper.writeValueAsString(any())).thenThrow(new JsonParseException("expectedException"));
    var ier = new ImportEventResult();

    // when
    var result = mapper.getFailedResources(ier, imports);

    // then
    assertThat(result).hasSize(1);
    var importEventFailedResource = result.iterator().next();
    assertThat(importEventFailedResource.getImportEventResult()).isEqualTo(ier);
    assertThat(importEventFailedResource.getRawResource()).isEqualTo("mapping to json failed: "
      + expectedException);
    assertThat(importEventFailedResource.getReason()).isEqualTo(failureReason);
  }

  private ImportUtils.ImportedResource getImportedResource(Long id, String label,
                                                           ImportUtils.Status status, String failureReason) {
    return new ImportUtils.ImportedResource(new Resource().setId(id).setLabel(label), status, failureReason);
  }
}
