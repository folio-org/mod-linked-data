package org.folio.linked.data.integration.kafka.listener;

import static org.folio.linked.data.util.Constants.STANDALONE_PROFILE;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.folio.linked.data.domain.dto.InventoryDomainHoldingEvent;
import org.folio.linked.data.domain.dto.InventoryDomainItemEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
@Profile("!" + STANDALONE_PROFILE)
public class InventoryDomainEventListener {

  private static final String ITEM_DOMAIN_EVENT_LISTENER = "mod-linked-data-inventory-item-domain-event-listener";
  private static final String HOLDING_DOMAIN_EVENT_LISTENER = "mod-linked-data-inventory-holding-domain-event-listener";

  @KafkaListener(
    id = ITEM_DOMAIN_EVENT_LISTENER,
    containerFactory = "itemListenerFactory",
    groupId = "#{folioKafkaProperties.listener['inventory-item-domain-event'].groupId}",
    concurrency = "#{folioKafkaProperties.listener['inventory-item-domain-event'].concurrency}",
    topicPattern = "#{folioKafkaProperties.listener['inventory-item-domain-event'].topicPattern}")
  public void handleInventoryItemDomainEvent(List<ConsumerRecord<String, InventoryDomainItemEvent>> consumerRecords) {
    consumerRecords.forEach(cr -> processRecord(cr, "Item"));
  }

  @KafkaListener(
    id = HOLDING_DOMAIN_EVENT_LISTENER,
    containerFactory = "holdingListenerFactory",
    groupId = "#{folioKafkaProperties.listener['inventory-holding-domain-event'].groupId}",
    concurrency = "#{folioKafkaProperties.listener['inventory-holding-domain-event'].concurrency}",
    topicPattern = "#{folioKafkaProperties.listener['inventory-holding-domain-event'].topicPattern}")
  public void handleInventoryHoldingDomainEvent(List<ConsumerRecord<String, InventoryDomainHoldingEvent>> records) {
    records.forEach(cr -> processRecord(cr, "Holding"));
  }

  private void processRecord(Object event, String entityType) {
    log.info("Received [{}] Domain Event: {}", entityType, event);
  }

}
