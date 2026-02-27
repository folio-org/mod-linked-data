package org.folio.linked.data.controller;

import static java.lang.String.valueOf;
import static org.folio.linked.data.util.Constants.STANDALONE_PROFILE;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.ReindexJobStatusDto;
import org.folio.linked.data.rest.resource.ReindexApi;
import org.folio.linked.data.service.batch.ReindexJobService;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@Profile("!" + STANDALONE_PROFILE)
public class ReindexingController implements ReindexApi {

  private final ReindexJobService reindexJobService;

  @Override
  public ResponseEntity<String> fullReindex(String resourceType) {
    var jobExecutionId = valueOf(reindexJobService.start(true, resourceType));
    return ResponseEntity.ok(jobExecutionId);
  }

  @Override
  public ResponseEntity<String> incrementalReindex(String resourceType) {
    var jobExecutionId = valueOf(reindexJobService.start(false, resourceType));
    return ResponseEntity.ok(jobExecutionId);
  }

  @Override
  public ResponseEntity<ReindexJobStatusDto> getReindexJobStatus(Long jobExecutionId) {
    return ResponseEntity.ok(reindexJobService.getStatus(jobExecutionId));
  }
}
