package org.folio.linked.data.service.batch;

import org.folio.linked.data.domain.dto.BatchJobStatusDto;

public interface BatchJobService {

  Long startReindex(boolean isFullReindex, String resourceType);

  Long startGraphCleaning(int executionRound);

  BatchJobStatusDto getStatus(Long jobExecutionId);
}
