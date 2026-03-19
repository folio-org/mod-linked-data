package org.folio.linked.data.controller;

import static java.lang.String.valueOf;
import static org.folio.linked.data.util.Constants.STANDALONE_PROFILE;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.BatchJobStatusDto;
import org.folio.linked.data.rest.resource.BatchJobApi;
import org.folio.linked.data.service.batch.BatchJobService;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@Profile("!" + STANDALONE_PROFILE)
public class BatchJobController implements BatchJobApi {

  private final BatchJobService batchJobService;

  @Override
  public ResponseEntity<String> fullReindex(String resourceType) {
    var jobExecutionId = valueOf(batchJobService.startReindex(true, resourceType));
    return ResponseEntity.ok(jobExecutionId);
  }

  @Override
  public ResponseEntity<String> incrementalReindex(String resourceType) {
    var jobExecutionId = valueOf(batchJobService.startReindex(false, resourceType));
    return ResponseEntity.ok(jobExecutionId);
  }

  @Override
  public ResponseEntity<String> graphCleaning() {
    var jobExecutionId = valueOf(batchJobService.startGraphCleaning(1));
    return ResponseEntity.ok(jobExecutionId);
  }

  @Override
  public ResponseEntity<BatchJobStatusDto> getBatchJobStatus(Long jobExecutionId) {
    return ResponseEntity.ok(batchJobService.getStatus(jobExecutionId));
  }
}
