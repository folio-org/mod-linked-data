package org.folio.linked.data.integration.kafka.listener.handler.srs;

import org.folio.linked.data.domain.dto.SourceRecordDomainEvent;
import org.folio.linked.data.domain.dto.SourceRecordType;

public interface SourceRecordDomainEventHandler {

  void handle(SourceRecordDomainEvent event, SourceRecordType recordType);
}
