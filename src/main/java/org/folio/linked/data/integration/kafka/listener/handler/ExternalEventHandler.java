package org.folio.linked.data.integration.kafka.listener.handler;

public interface ExternalEventHandler<T> {

  void handle(T event);
}
