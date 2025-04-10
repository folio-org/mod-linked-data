package org.folio.linked.data.integration.kafka.listener;

import static org.folio.linked.data.test.TestUtil.TENANT_ID;
import static org.folio.linked.data.test.TestUtil.awaitAndAssert;
import static org.folio.linked.data.test.kafka.KafkaEventsTestDataFixture.getInventoryInstanceEventSampleProducerRecord;
import static org.folio.spring.integration.XOkapiHeaders.TENANT;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.folio.linked.data.domain.dto.InventoryInstanceEvent;
import org.folio.linked.data.domain.dto.ResourceIndexEventType;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.linked.data.integration.kafka.listener.handler.InventoryInstanceEventHandler;
import org.folio.spring.tools.kafka.KafkaAdminService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

@IntegrationTest
class InventoryInstanceEventListenerIT {

  @Autowired
  private KafkaTemplate<String, String> eventKafkaTemplate;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoSpyBean
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

  @Test
  void shouldNotConsumeInventoryInstanceEvent_whenModuleNotInstalled() {
    // given
    var eventProducerRecord = getInventoryInstanceEventSampleProducerRecord();
    eventProducerRecord.headers().remove(TENANT);
    eventProducerRecord.headers().add(new RecordHeader(TENANT, "some-tenant-without-linked-data-module".getBytes()));

    // when
    eventKafkaTemplate.send(eventProducerRecord);

    // then
    awaitAndAssert(() -> verifyNoInteractions(inventoryInstanceDomainEventHandler));
  }
}
