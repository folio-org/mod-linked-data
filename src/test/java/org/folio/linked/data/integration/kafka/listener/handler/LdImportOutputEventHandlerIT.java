package org.folio.linked.data.integration.kafka.listener.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.linked.data.test.TestUtil.LD_IMPORT_OUTPUT_TOPIC;
import static org.folio.linked.data.test.TestUtil.TENANT_ID;
import static org.folio.linked.data.test.TestUtil.awaitAndAssert;
import static org.folio.linked.data.test.TestUtil.cleanResourceTables;
import static org.folio.linked.data.test.TestUtil.defaultKafkaHeaders;
import static org.folio.linked.data.test.TestUtil.loadResourceAsString;
import static org.folio.spring.tools.kafka.KafkaUtils.getTenantTopicName;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.ArrayList;
import lombok.SneakyThrows;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.linked.data.mapper.ResourceModelMapper;
import org.folio.linked.data.service.tenant.TenantScopedExecutionService;
import org.folio.linked.data.test.kafka.KafkaImportResultTopicListener;
import org.folio.linked.data.test.resource.ResourceTestRepository;
import org.folio.spring.tools.kafka.KafkaAdminService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;

@IntegrationTest
class LdImportOutputEventHandlerIT {

  @Autowired
  private KafkaTemplate<String, String> eventKafkaTemplate;
  @Autowired
  private TenantScopedExecutionService tenantScopedExecutionService;
  @Autowired
  private ResourceTestRepository resourceRepository;
  @Autowired
  private JdbcTemplate jdbcTemplate;
  @Autowired
  private ResourceModelMapper resourceModelMapper;
  @Autowired
  private KafkaImportResultTopicListener importResultListener;

  @BeforeAll
  static void beforeAll(@Autowired KafkaAdminService kafkaAdminService) {
    kafkaAdminService.createTopics(TENANT_ID);
  }

  @BeforeEach
  void clean() {
    tenantScopedExecutionService.execute(TENANT_ID, () -> cleanResourceTables(jdbcTemplate));
    importResultListener.clear();
  }

  @Test
  void incomingLdImportEvent_shouldBeHandledCorrectly() {
    // given
    var ldImportOutputEvent = getImportOutputEventProducerRecord();
    var id = -1653433756816039203L;

    // when
    eventKafkaTemplate.send(ldImportOutputEvent);

    // then
    awaitAndAssert(() -> assertFalse(importResultListener.getMessages().isEmpty()));

    var resourceSavedOpt = tenantScopedExecutionService.execute(TENANT_ID,
      () -> resourceRepository.findByIdWithEdgesLoaded(id)
    );
    assertThat(resourceSavedOpt).isPresent();
    var resourceSaved = resourceSavedOpt.get();
    assertThat(resourceSaved.getTypes()).isNotEmpty();
    assertThat(resourceSaved.getOutgoingEdges()).isNotEmpty();

    var importResultMessages = importResultListener.getMessages();
    assertThat(importResultMessages).hasSize(1);

    var eventResult = importResultMessages.getFirst();
    assertThat(eventResult.getJobInstanceId()).isEqualTo(123L);
    assertThat(eventResult.getResourcesCount()).isEqualTo(1);
    assertThat(eventResult.getCreatedCount()).isEqualTo(1);
    assertThat(eventResult.getUpdatedCount()).isZero();
    assertThat(eventResult.getFailedResources()).isEmpty();
  }

  @SneakyThrows
  private ProducerRecord<String, String> getImportOutputEventProducerRecord() {
    var headers = new ArrayList<>(defaultKafkaHeaders());
    var topic = getTenantTopicName(LD_IMPORT_OUTPUT_TOPIC, TENANT_ID);
    var value = loadResourceAsString("samples/importOutputEvent.json");
    return new ProducerRecord(topic, 0, "1", value, headers);
  }

}
