package org.folio.linked.data.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.PropertyDictionary.EDITION_STATEMENT;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.linked.data.test.TestUtil.FOLIO_TEST_PROFILE;
import static org.folio.linked.data.test.TestUtil.TENANT_ID;
import static org.folio.linked.data.test.TestUtil.defaultKafkaHeaders;
import static org.folio.linked.data.test.TestUtil.loadResourceAsString;
import static org.folio.linked.data.util.Constants.FOLIO_PROFILE;
import static org.folio.linked.data.utils.KafkaEventsTestDataFixture.dataImportEvent;
import static org.folio.spring.tools.config.properties.FolioEnvironment.getFolioEnvName;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;
import static org.testcontainers.shaded.org.awaitility.Durations.FIVE_SECONDS;
import static org.testcontainers.shaded.org.awaitility.Durations.ONE_HUNDRED_MILLISECONDS;
import static org.testcontainers.shaded.org.awaitility.Durations.ONE_MINUTE;

import java.util.Set;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.linked.data.integration.consumer.DataImportEventHandler;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.repo.ResourceRepository;
import org.folio.linked.data.service.tenant.TenantScopedExecutionService;
import org.folio.linked.data.test.ResourceEdgeRepository;
import org.folio.search.domain.dto.DataImportEvent;
import org.folio.spring.tools.kafka.KafkaAdminService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@IntegrationTest
@ActiveProfiles({FOLIO_PROFILE, FOLIO_TEST_PROFILE})
class DataImportEventListenerIT {

  private static final String DI_INSTANCE_CREATED_TOPIC = "DI_INVENTORY_INSTANCE_CREATED_READY_FOR_POST_PROCESSING";
  @Autowired
  private ResourceRepository resourceRepo;
  @Autowired
  private ResourceEdgeRepository resourceEdgeRepository;
  @Autowired
  private KafkaTemplate<String, String> eventKafkaTemplate;
  @SpyBean
  @Autowired
  private DataImportEventHandler dataImportEventHandler;
  @Autowired
  private TenantScopedExecutionService tenantScopedExecutionService;

  @BeforeAll
  static void beforeAll(@Autowired KafkaAdminService kafkaAdminService) {
    kafkaAdminService.createTopics(TENANT_ID);
  }

  private static String getTopicName(String tenantId, String topic) {
    return String.format("%s.%s.%s", getFolioEnvName(), tenantId, topic);
  }

  @AfterEach
  public void clean() {
    resourceEdgeRepository.deleteAll();
    resourceRepo.deleteAll();
  }

  @Test
  void shouldConsumeInstanceCreatedEventFromDataImport() {
    // given
    var eventId = "event_id_01";
    var eventType = "eventType_01";
    var marc = loadResourceAsString("samples/full_marc_sample.jsonl");
    var emittedEvent = dataImportEvent(eventId, TENANT_ID, eventType, marc);
    var expectedEvent = new DataImportEvent()
      .id(eventId)
      .tenant(TENANT_ID)
      .eventType(eventType)
      .marc(marc);
    var producerRecord = new ProducerRecord(getTopicName(TENANT_ID, DI_INSTANCE_CREATED_TOPIC), 0,
      eventId, emittedEvent, defaultKafkaHeaders());

    // when
    eventKafkaTemplate.send(producerRecord);

    // then
    await().atMost(ONE_MINUTE)
      .pollDelay(FIVE_SECONDS)
      .pollInterval(ONE_HUNDRED_MILLISECONDS)
      .untilAsserted(() -> verify(dataImportEventHandler, times(1)).handle(expectedEvent));

    var found = tenantScopedExecutionService.executeTenantScoped(
      TENANT_ID,
      () -> resourceRepo.findResourcesByTypeFull(Set.of(INSTANCE.getUri()), Pageable.ofSize(1))
        .stream()
        .findFirst()
    );
    assertThat(found).isPresent();
    var result = found.get();
    assertThat(result.getLabel()).isEqualTo("Instance MainTitle");
    assertThat(result.getInventoryId()).hasToString("2165ef4b-001f-46b3-a60e-52bcdeb3d5a1");
    assertThat(result.getSrsId()).hasToString("43d58061-decf-4d74-9747-0e1c368e861b");
    assertThat(result.getTypes().iterator().next().getUri()).isEqualTo(INSTANCE.getUri());
    assertThat(result.getDoc()).hasSize(1);
    assertThat(result.getDoc().has(EDITION_STATEMENT.getValue())).isTrue();
    assertThat(result.getDoc().get(EDITION_STATEMENT.getValue())).hasSize(1);
    assertThat(result.getDoc().get(EDITION_STATEMENT.getValue()).get(0).asText())
      .isEqualTo("Edition Statement Edition statement2");
    assertThat(result.getOutgoingEdges()).hasSize(8);
    for (ResourceEdge edge : result.getOutgoingEdges()) {
      assertThat(edge.getSource()).isEqualTo(result);
      assertThat(edge.getTarget()).isNotNull();
      assertThat(edge.getPredicate()).isNotNull();
    }
  }

}
