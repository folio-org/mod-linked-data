package org.folio.linked.data.integration.kafka.listener.handler;

import java.time.OffsetDateTime;

public interface ExternalEventHandler<T> {

  void handle(T event, OffsetDateTime startTime);
}
