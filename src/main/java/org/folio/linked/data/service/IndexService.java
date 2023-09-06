package org.folio.linked.data.service;

import org.folio.linked.data.domain.dto.IndexRequest;
import org.folio.linked.data.domain.dto.IndexResponse;

public interface IndexService {

  IndexResponse createIndex(IndexRequest request);

}
