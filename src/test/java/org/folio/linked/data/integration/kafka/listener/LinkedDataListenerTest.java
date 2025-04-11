package org.folio.linked.data.integration.kafka.listener;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.folio.linked.data.test.TestUtil.TENANT_ID;
import static org.folio.spring.integration.XOkapiHeaders.TENANT;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.function.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.logging.log4j.Logger;
import org.folio.linked.data.domain.dto.InventoryInstanceEvent;
import org.folio.linked.data.service.tenant.LinkedDataTenantService;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class LinkedDataListenerTest {

  @Mock
  private LinkedDataTenantService linkedDataTenantService;

  @Mock
  private Logger logger;

  @Mock
  private ConsumerRecord<String, InventoryInstanceEvent> consumerRecord;

  @Mock
  private Headers headers;

  @Mock
  private Consumer<ConsumerRecord<String, InventoryInstanceEvent>> consumer;

  private final LinkedDataListener<InventoryInstanceEvent> listener = InventoryInstanceEvent::getId;
  private final String eventId = "some-id";

  @Test
  void handle_shouldThrowException_whenTenantHeaderIsMissing() {
    // given
    when(consumerRecord.value()).thenReturn(new InventoryInstanceEvent(eventId));
    when(consumerRecord.headers()).thenReturn(headers);
    when(headers.lastHeader(TENANT)).thenReturn(null);
    var consumerRecords = List.of(consumerRecord);

    // when
    var thrown = assertThrows(IllegalArgumentException.class,
      () -> listener.handle(consumerRecords, System.out::println, linkedDataTenantService, logger));

    // then
    assertThat(thrown.getMessage())
      .isEqualTo("Received InventoryInstanceEvent [id some-id] is missing x-okapi-tenant header");
  }

  @Test
  void handle_shouldSkipEvent_whenModuleIsNotInstalled() {
    // given
    when(consumerRecord.value()).thenReturn(new InventoryInstanceEvent(eventId));
    when(consumerRecord.headers()).thenReturn(headers);
    when(headers.lastHeader(TENANT))
      .thenReturn(new RecordHeader(TENANT, TENANT_ID.getBytes()));
    when(linkedDataTenantService.isTenantExists(TENANT_ID)).thenReturn(false);

    // when
    listener.handle(List.of(consumerRecord), System.out::println, linkedDataTenantService, logger);

    // then
    verify(logger).debug("Received {} [id {}] will be ignored since module is not installed on tenant {}",
      "InventoryInstanceEvent", eventId, TENANT_ID);
  }

  @Test
  void handle_shouldHandleEvent() {
    // given
    when(consumerRecord.value()).thenReturn(new InventoryInstanceEvent(eventId));
    when(consumerRecord.headers()).thenReturn(headers);
    when(headers.lastHeader(TENANT))
      .thenReturn(new RecordHeader(TENANT, TENANT_ID.getBytes()));
    when(linkedDataTenantService.isTenantExists(TENANT_ID)).thenReturn(true);

    // when
    listener.handle(List.of(consumerRecord), consumer, linkedDataTenantService, logger);

    // then
    verify(consumer).accept(consumerRecord);
  }
}
