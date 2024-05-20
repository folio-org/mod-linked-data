package org.folio.linked.data.integration;

import static org.folio.linked.data.test.KafkaEventsTestDataFixture.instanceCreatedEvent;
import static org.folio.linked.data.test.TestUtil.FOLIO_TEST_PROFILE;
import static org.folio.linked.data.test.TestUtil.awaitAndAssert;
import static org.folio.linked.data.test.TestUtil.defaultKafkaHeaders;
import static org.folio.linked.data.util.Constants.FOLIO_PROFILE;
import static org.folio.spring.tools.config.properties.FolioEnvironment.getFolioEnvName;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.linked.data.integration.kafka.consumer.DataImportEventHandler;
import org.folio.search.domain.dto.DataImportEvent;
import org.folio.spring.tools.kafka.KafkaAdminService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;

@IntegrationTest
@ActiveProfiles({FOLIO_PROFILE, FOLIO_TEST_PROFILE})
class KafkaMessageListenerIT {

  private static final String TENANT_ID = "tenant_01";
  private static final String DI_COMPLETED_TOPIC = "DI_COMPLETED";

  @Autowired
  private KafkaTemplate<String, String> eventKafkaTemplate;

  @MockBean
  private DataImportEventHandler dataImportEventConsumer;

  @BeforeAll
  static void setup(@Autowired KafkaAdminService kafkaAdminService) {
    kafkaAdminService.createTopics(TENANT_ID);
  }

  @Test
  void shouldConsumeInstanceCreatedEventFromDataImport() {
    String eventId = "event_id_01";
    String tenantId = "tenant_01";
    String marc = "{}";

    String emittedEvent = instanceCreatedEvent(eventId, tenantId, marc);
    DataImportEvent expectedEvent = new DataImportEvent()
      .id(eventId)
      .tenant(tenantId)
      .eventType("DI_COMPLETED")
      .marcBib(marc);

    var producerRecord = new ProducerRecord(getTopicName(TENANT_ID, DI_COMPLETED_TOPIC), 0,
      eventId, emittedEvent, defaultKafkaHeaders());

    eventKafkaTemplate.send(producerRecord);
    awaitAndAssert(() -> verify(dataImportEventConsumer, times(1)).handle(expectedEvent));
  }

  private String getTopicName(String tenantId, String topic) {
    return String.format("%s.%s.%s", getFolioEnvName(), tenantId, topic);
  }
}
