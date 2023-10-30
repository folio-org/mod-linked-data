package org.folio.linked.data.integration;

import static org.folio.linked.data.test.TestUtil.FOLIO_TEST_PROFILE;
import static org.folio.linked.data.util.Constants.FOLIO_PROFILE;
import static org.folio.linked.data.utils.KafkaEventsTestDataFixture.dataImportEvent;
import static org.folio.spring.tools.config.properties.FolioEnvironment.getFolioEnvName;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;
import static org.testcontainers.shaded.org.awaitility.Durations.FIVE_SECONDS;
import static org.testcontainers.shaded.org.awaitility.Durations.ONE_HUNDRED_MILLISECONDS;

import org.folio.linked.data.configuration.KafkaListenerConfiguration;
import org.folio.linked.data.configuration.json.ObjectMapperConfig;
import org.folio.linked.data.integration.consumer.DataImportEventHandler;
import org.folio.search.domain.dto.DataImportEvent;
import org.folio.spring.test.extension.EnableKafka;
import org.folio.spring.test.type.IntegrationTest;
import org.folio.spring.tools.kafka.KafkaAdminService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;

@EnableKafka
@IntegrationTest
@Import({
  KafkaAdminService.class,
  KafkaAutoConfiguration.class,
  KafkaListenerConfiguration.class,
  ObjectMapperConfig.class
})
@SpringBootTest(classes = {KafkaMessageListener.class})
@ActiveProfiles({FOLIO_PROFILE, FOLIO_TEST_PROFILE})
class KafkaMessageListenerIT {

  private static final String TENANT_ID = "tenant_01";
  private static final String DI_INSTANCE_CREATED_TOPIC = "DI_INVENTORY_INSTANCE_CREATED_READY_FOR_POST_PROCESSING";

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
    String eventType = "eventType_01";
    String tenantId = "tenant_01";
    String marc = "{}";

    String emittedEvent = dataImportEvent(eventId, tenantId, eventType, marc);
    DataImportEvent expectedEvent = new DataImportEvent()
      .id(eventId)
      .tenant(tenantId)
      .eventType(eventType)
      .marc(marc);

    eventKafkaTemplate.send(getTopicName(TENANT_ID, DI_INSTANCE_CREATED_TOPIC), eventId, emittedEvent);
    await().atMost(FIVE_SECONDS)
      .pollInterval(ONE_HUNDRED_MILLISECONDS)
      .untilAsserted(() -> verify(dataImportEventConsumer, times(1)).handle(expectedEvent));
  }

  private String getTopicName(String tenantId, String topic) {
    return String.format("%s.%s.%s", getFolioEnvName(), tenantId, topic);
  }
}
