package org.folio.linked.data.service.batch;

import org.folio.linked.data.domain.dto.ReindexJobStatusDto;

public interface ReindexJobService {

  Long start(boolean isFullReindex, String resourceType);

  ReindexJobStatusDto getStatus(Long jobExecutionId);
}
