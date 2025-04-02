package org.folio.linked.data.integration.kafka.listener;

import static org.folio.linked.data.test.TestUtil.TENANT_ID;
import static org.folio.linked.data.test.kafka.KafkaEventsTestDataFixture.getInventoryDomainEventHoldingSampleProducerRecord;
import static org.folio.linked.data.test.kafka.KafkaEventsTestDataFixture.getInventoryDomainEventItemSampleProducerRecord;

import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.spring.tools.kafka.KafkaAdminService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;

@IntegrationTest
class InventoryDomainEventListenerIT {

  @Autowired
  private KafkaTemplate<String, String> eventKafkaTemplate;

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
    // check logs, that's enough for Spike
  }

  @Test
  void shouldHandleHoldingDomainEvent() {
    // given
    var eventProducerRecord = getInventoryDomainEventHoldingSampleProducerRecord();

    // when
    eventKafkaTemplate.send(eventProducerRecord);

    // then
    // check logs, that's enough for Spike
  }

}
