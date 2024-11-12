package org.folio.linked.data.integration.kafka.listener;

import static java.util.Optional.ofNullable;
import static org.folio.linked.data.domain.dto.ResourceIndexEventType.UPDATE;
import static org.folio.linked.data.util.Constants.STANDALONE_PROFILE;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.logging.log4j.Level;
import org.folio.linked.data.domain.dto.InventoryInstanceEvent;
import org.folio.linked.data.integration.kafka.listener.handler.InventoryInstanceEventHandler;
import org.folio.linked.data.service.tenant.TenantScopedExecutionService;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.retry.RetryContext;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
@Profile("!" + STANDALONE_PROFILE)
public class InventoryInstanceEventListener {

  private static final String INVENTORY_INSTANCE_EVENT_LISTENER = "mod-linked-data-inventory-instance-event-listener";
  private static final String INVENTORY_EVENT_LISTENER_CONTAINER_FACTORY = "inventoryEventListenerContainerFactory";
  private final TenantScopedExecutionService tenantScopedExecutionService;
  private final InventoryInstanceEventHandler inventoryInstanceEventHandler;

  @KafkaListener(
    id = INVENTORY_INSTANCE_EVENT_LISTENER,
    containerFactory = INVENTORY_EVENT_LISTENER_CONTAINER_FACTORY,
    groupId = "#{folioKafkaProperties.listener['inventory-instance-event'].groupId}",
    concurrency = "#{folioKafkaProperties.listener['inventory-instance-event'].concurrency}",
    topicPattern = "#{folioKafkaProperties.listener['inventory-instance-event'].topicPattern}")
  public void handleInventoryInstanceEvent(List<ConsumerRecord<String, InventoryInstanceEvent>> consumerRecords) {
    consumerRecords.forEach(this::handleRecord);
  }

  private void handleRecord(ConsumerRecord<String, InventoryInstanceEvent> consumerRecord) {
    var event = consumerRecord.value();
    if (event.getType() == UPDATE) {
      tenantScopedExecutionService.executeAsyncWithRetry(
        consumerRecord.headers(),
        retryContext -> runRetryableJob(event, retryContext),
        ex -> logFailedEvent(event, ex, false)
      );
    }
  }

  private void runRetryableJob(InventoryInstanceEvent event, RetryContext retryContext) {
    ofNullable(retryContext.getLastThrowable())
      .ifPresent(ex -> logFailedEvent(event, ex, true));
    inventoryInstanceEventHandler.handle(event);
  }

  private void logFailedEvent(InventoryInstanceEvent event, Throwable ex, boolean isRetrying) {
    var logLevel = isRetrying ? Level.INFO : Level.ERROR;
    log.log(logLevel, "Failed to reindex inventory instance with id {}. Retrying: {}",
      event.getNew().getId(), isRetrying, ex);
  }
}
