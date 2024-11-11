package org.folio.linked.data.integration.kafka.listener;

import static org.folio.linked.data.domain.dto.SourceRecordDomainEvent.EventTypeEnum.CREATED;
import static org.folio.linked.data.domain.dto.SourceRecordType.MARC_AUTHORITY;
import static org.folio.linked.data.domain.dto.SourceRecordType.MARC_BIB;
import static org.folio.linked.data.test.TestUtil.TENANT_ID;
import static org.folio.linked.data.test.TestUtil.awaitAndAssert;
import static org.folio.linked.data.test.kafka.KafkaEventsTestDataFixture.getSrsDomainEvent;
import static org.folio.linked.data.test.kafka.KafkaEventsTestDataFixture.getSrsDomainEventProducerRecord;
import static org.folio.linked.data.test.kafka.KafkaEventsTestDataFixture.getSrsDomainEventSampleProducerRecord;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.folio.linked.data.domain.dto.ParsedRecord;
import org.folio.linked.data.domain.dto.SourceRecord;
import org.folio.linked.data.domain.dto.SourceRecordDomainEvent;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.linked.data.integration.kafka.listener.handler.SourceRecordDomainEventHandler;
import org.folio.linked.data.test.TestUtil;
import org.folio.spring.tools.kafka.KafkaAdminService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;

@IntegrationTest
class SourceRecordDomainEventListenerIT {

  @Autowired
  private KafkaTemplate<String, String> eventKafkaTemplate;

  @MockBean
  private SourceRecordDomainEventHandler sourceRecordDomainEventHandler;

  @BeforeAll
  static void setup(@Autowired KafkaAdminService kafkaAdminService) {
    kafkaAdminService.createTopics(TENANT_ID);
  }

  @Disabled("Handling MARC BIB records is disabled temporarily")
  @Test
  void shouldConsumeSrsDomainEvent() {
    // given
    var eventProducerRecord = getSrsDomainEventSampleProducerRecord();
    var expectedEvent = new SourceRecordDomainEvent("23b34a6f-c095-41ea-917b-9765add1a444", CREATED).eventPayload(
      new SourceRecord()
        .id("758f57ff-7dad-419e-803c-44b621400c11")
        .parsedRecord(new ParsedRecord()
          .content(TestUtil.loadResourceAsString("samples/srsDomainEventParsedContent.txt"))
        )
        .deleted(true)
    );

    // when
    eventKafkaTemplate.send(eventProducerRecord);

    // then
    awaitAndAssert(() -> verify(sourceRecordDomainEventHandler).handle(expectedEvent, MARC_BIB));
  }

  @Disabled("Handling MARC BIB records is disabled temporarily")
  @Test
  void shouldRetryIfErrorOccurs() {
    // given
    var eventId = "event_id_02";
    var marc = "{}";
    var recordType = MARC_BIB;
    var eventType = CREATED;
    var eventProducerRecord = getSrsDomainEventProducerRecord(eventId, marc, eventType, recordType);
    var expectedEvent = getSrsDomainEvent(eventId, marc, eventType);
    doThrow(new RuntimeException("An error occurred"))
      .doNothing()
      .when(sourceRecordDomainEventHandler).handle(expectedEvent, recordType);

    // when
    eventKafkaTemplate.send(eventProducerRecord);

    // then
    awaitAndAssert(() -> verify(sourceRecordDomainEventHandler, times(2)).handle(expectedEvent, recordType));
  }

  @Test
  void shouldNotHandleSrsDomainEvent_whenSourceRecordType_isMarcBib() {
    // given
    var eventProducerRecord = getSrsDomainEventProducerRecord("1", "{}", CREATED, MARC_BIB);

    // when
    eventKafkaTemplate.send(eventProducerRecord);

    // then
    awaitAndAssert(() -> verifyNoInteractions(sourceRecordDomainEventHandler));
  }

  @Test
  void shouldHandleSrsDomainEvent_whenSourceRecordType_isMarcAuthority() {
    // given
    var eventId = "2";
    var marc = "{}";
    var eventType = CREATED;
    var eventProducerRecord = getSrsDomainEventProducerRecord(eventId, marc, eventType, MARC_AUTHORITY);
    var expectedEvent = getSrsDomainEvent(eventId, marc, eventType);

    // when
    eventKafkaTemplate.send(eventProducerRecord);

    // then
    awaitAndAssert(() -> verify(sourceRecordDomainEventHandler).handle(expectedEvent, MARC_AUTHORITY));
  }

}
