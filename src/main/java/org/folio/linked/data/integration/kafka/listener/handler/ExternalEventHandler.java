package org.folio.linked.data.integration.kafka.listener.handler;

import java.time.LocalDateTime;

public interface ExternalEventHandler<T> {

  void handle(T event, LocalDateTime startTime);
}
