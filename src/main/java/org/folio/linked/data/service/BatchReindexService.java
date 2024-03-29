package org.folio.linked.data.service;

import java.util.Set;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.data.domain.Page;

public interface BatchReindexService {

  BatchReindexResult batchReindex(Page<Resource> page);

  record BatchReindexResult(int recordsIndexed, Set<Long> indexedIds) {}
}
