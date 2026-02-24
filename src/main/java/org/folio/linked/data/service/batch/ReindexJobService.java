package org.folio.linked.data.service.batch;

public interface ReindexJobService {

  Long start(boolean isFullReindex, String resourceType);
}
