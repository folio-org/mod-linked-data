package org.folio.linked.data.service;

import org.folio.search.domain.dto.BibframeIndex;

public interface KafkaSender {

  void sendResourceCreated(BibframeIndex bibframeIndex);

  void sendResourceDeleted(Long id);

}
