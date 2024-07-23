package org.folio.linked.data.service;

import java.util.Set;
import java.util.stream.Stream;
import org.folio.linked.data.model.entity.Resource;

public interface BatchIndexService {

  BatchIndexResult indexWorks(Stream<Resource> works);

  record BatchIndexResult(int recordsIndexed, Set<Long> indexedIds) {}
}
