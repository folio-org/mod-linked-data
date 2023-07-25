package org.folio.linked.data.service;

import org.folio.linked.data.domain.dto.BibframeRequest;

public interface KafkaSender {

  void sendResourceCreated(String tenant, BibframeRequest request, Long id);

}
