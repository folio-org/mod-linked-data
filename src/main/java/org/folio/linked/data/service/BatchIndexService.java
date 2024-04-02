package org.folio.linked.data.service;

import java.util.Set;
import java.util.stream.Stream;
import org.folio.linked.data.model.entity.Resource;

public interface BatchIndexService {

  BatchIndexResult index(Stream<Resource> resources);

  record BatchIndexResult(int recordsIndexed, Set<Long> indexedIds) {}
}
