package org.folio.linked.data.integration.kafka.listener;

import static org.folio.linked.data.test.TestUtil.TENANT_ID;
import static org.folio.linked.data.test.TestUtil.awaitAndAssert;
import static org.folio.linked.data.test.kafka.KafkaEventsTestDataFixture.getInventoryInstanceEventSampleProducerRecord;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.folio.linked.data.domain.dto.InventoryInstanceEvent;
import org.folio.linked.data.domain.dto.ResourceIndexEventType;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.linked.data.integration.kafka.listener.handler.InventoryInstanceEventHandler;
import org.folio.spring.tools.kafka.KafkaAdminService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;

@IntegrationTest
class InventoryInstanceEventListenerIT {

  @Autowired
  private KafkaTemplate<String, String> eventKafkaTemplate;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private InventoryInstanceEventHandler inventoryInstanceDomainEventHandler;

  @BeforeAll
  static void setup(@Autowired KafkaAdminService kafkaAdminService) {
    kafkaAdminService.createTopics(TENANT_ID);
  }

  @Test
  void shouldConsumeInventoryInstanceEvent() {
    // given
    var eventProducerRecord = getInventoryInstanceEventSampleProducerRecord();

    // when
    eventKafkaTemplate.send(eventProducerRecord);

    // then
    awaitAndAssert(() -> verify(inventoryInstanceDomainEventHandler)
      .handle(objectMapper.readValue(eventProducerRecord.value(), InventoryInstanceEvent.class)));
  }

  @Test
  void shouldRetryIfErrorOccurs() throws JsonProcessingException {
    // given
    var eventProducerRecord = getInventoryInstanceEventSampleProducerRecord();
    var event = objectMapper.readValue(eventProducerRecord.value(), InventoryInstanceEvent.class);
    var expectedEvent = new InventoryInstanceEvent()
      .id(event.getId())
      ._new(event.getNew())
      .old(event.getOld())
      .tenant(event.getTenant())
      .type(ResourceIndexEventType.UPDATE);
    doThrow(new RuntimeException("An error occurred"))
      .doNothing()
      .when(inventoryInstanceDomainEventHandler).handle(expectedEvent);

    // when
    eventKafkaTemplate.send(eventProducerRecord);

    // then
    awaitAndAssert(() -> verify(inventoryInstanceDomainEventHandler, times(2))
      .handle(expectedEvent));
  }
}
