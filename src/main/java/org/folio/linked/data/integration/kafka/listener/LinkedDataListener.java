package org.folio.linked.data.integration.kafka.listener;

import static org.folio.linked.data.util.KafkaUtils.getHeaderValueByName;
import static org.folio.spring.integration.XOkapiHeaders.TENANT;

import java.util.List;
import java.util.function.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.logging.log4j.Logger;
import org.folio.linked.data.service.ApplicationService;

public interface LinkedDataListener<T> {

  String getEventId(T event);

  default void handle(List<ConsumerRecord<String, T>> consumerRecords,
                      Consumer<ConsumerRecord<String, T>> handler,
                      ApplicationService applicationService,
                      Logger log) {
    consumerRecords.forEach(consumerRecord -> {
      var event = consumerRecord.value();
      var eventName = event.getClass().getSimpleName();
      var eventId = getEventId(event);
      var tenant = getHeaderValueByName(consumerRecord, TENANT)
        .orElseThrow(() -> new IllegalArgumentException("Received %s [id %s] is missing x-okapi-tenant header"
          .formatted(eventName, eventId)));

      if (applicationService.isModuleInstalled(tenant)) {
        handler.accept(consumerRecord);
      } else {
        log.debug("Received {} [id {}] will be ignored since module is not installed on tenant {}",
          eventName, eventId, tenant);
      }
    });
  }
}
