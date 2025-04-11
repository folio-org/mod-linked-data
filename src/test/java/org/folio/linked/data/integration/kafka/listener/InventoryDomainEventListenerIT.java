package org.folio.linked.data.integration.kafka.listener;

import static org.folio.linked.data.test.TestUtil.TENANT_ID;
import static org.folio.linked.data.test.TestUtil.awaitAndAssert;
import static org.folio.linked.data.test.kafka.KafkaEventsTestDataFixture.getInventoryDomainEventHoldingSampleProducerRecord;
import static org.folio.linked.data.test.kafka.KafkaEventsTestDataFixture.getInventoryDomainEventItemSampleProducerRecord;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.spring.tools.kafka.KafkaAdminService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

@IntegrationTest
class InventoryDomainEventListenerIT {

  @Autowired
  private KafkaTemplate<String, String> eventKafkaTemplate;
  @MockitoSpyBean
  private InventoryDomainEventListener inventoryDomainEventListener;

  @BeforeAll
  static void setup(@Autowired KafkaAdminService kafkaAdminService) {
    kafkaAdminService.createTopics(TENANT_ID);
  }

  @Test
  void shouldHandleItemDomainEvent() {
    // given
    var eventProducerRecord = getInventoryDomainEventItemSampleProducerRecord();

    // when
    eventKafkaTemplate.send(eventProducerRecord);

    // then
    awaitAndAssert(() -> verify(inventoryDomainEventListener).handleInventoryItemDomainEvent(any()));
  }

  @Test
  void shouldHandleHoldingDomainEvent() {
    // given
    var eventProducerRecord = getInventoryDomainEventHoldingSampleProducerRecord();

    // when
    eventKafkaTemplate.send(eventProducerRecord);

    // then
    awaitAndAssert(() -> verify(inventoryDomainEventListener).handleInventoryHoldingDomainEvent(any()));
  }

}
