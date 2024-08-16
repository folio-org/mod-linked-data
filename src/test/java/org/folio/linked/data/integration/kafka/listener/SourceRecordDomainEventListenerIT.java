package org.folio.linked.data.integration.kafka.listener;

import static org.folio.linked.data.test.TestUtil.FOLIO_TEST_PROFILE;
import static org.folio.linked.data.test.TestUtil.TENANT_ID;
import static org.folio.linked.data.test.TestUtil.awaitAndAssert;
import static org.folio.linked.data.test.kafka.KafkaEventsTestDataFixture.getSrsDomainEvent;
import static org.folio.linked.data.test.kafka.KafkaEventsTestDataFixture.getSrsDomainEventProducerRecord;
import static org.folio.linked.data.util.Constants.DISABLED_FOR_BETA;
import static org.folio.linked.data.util.Constants.FOLIO_PROFILE;
import static org.folio.search.domain.dto.SourceRecordDomainEvent.EventTypeEnum.CREATED;
import static org.folio.search.domain.dto.SourceRecordType.MARC_BIB;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.linked.data.integration.kafka.listener.handler.SourceRecordDomainEventHandler;
import org.folio.spring.tools.kafka.KafkaAdminService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;

@IntegrationTest
@ActiveProfiles({FOLIO_PROFILE, FOLIO_TEST_PROFILE, DISABLED_FOR_BETA})
class SourceRecordDomainEventListenerIT {

  @Autowired
  private KafkaTemplate<String, String> eventKafkaTemplate;

  @MockBean
  private SourceRecordDomainEventHandler sourceRecordDomainEventHandler;

  @BeforeAll
  static void setup(@Autowired KafkaAdminService kafkaAdminService) {
    kafkaAdminService.createTopics(TENANT_ID);
  }

  @Test
  void shouldConsumeSrsDomainEvent() {
    // given
    var eventId = "event_id_01";
    var marc = "{}";
    var recordType = MARC_BIB;
    var eventType = CREATED;
    var eventProducerRecord = getSrsDomainEventProducerRecord(eventId, marc, eventType, recordType);
    var expectedEvent = getSrsDomainEvent(eventId, marc, eventType);

    // when
    eventKafkaTemplate.send(eventProducerRecord);

    // then
    awaitAndAssert(() -> verify(sourceRecordDomainEventHandler).handle(expectedEvent, recordType));
  }

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

}
