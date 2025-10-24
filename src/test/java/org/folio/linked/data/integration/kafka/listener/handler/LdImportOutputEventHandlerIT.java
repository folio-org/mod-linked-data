package org.folio.linked.data.integration.kafka.listener.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.linked.data.test.MonographTestUtil.getSimpleInstanceModel;
import static org.folio.linked.data.test.TestUtil.LD_IMPORT_OUTPUT_TOPIC;
import static org.folio.linked.data.test.TestUtil.OBJECT_MAPPER;
import static org.folio.linked.data.test.TestUtil.TENANT_ID;
import static org.folio.linked.data.test.TestUtil.awaitAndAssert;
import static org.folio.linked.data.test.TestUtil.cleanResourceTables;
import static org.folio.linked.data.test.TestUtil.defaultKafkaHeaders;
import static org.folio.spring.tools.kafka.KafkaUtils.getTenantTopicName;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.SneakyThrows;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.folio.ld.dictionary.model.Resource;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.linked.data.mapper.ResourceModelMapper;
import org.folio.linked.data.service.tenant.TenantScopedExecutionService;
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

  @BeforeAll
  static void beforeAll(@Autowired KafkaAdminService kafkaAdminService) {
    kafkaAdminService.createTopics(TENANT_ID);
  }

  @BeforeEach
  void clean() {
    tenantScopedExecutionService.execute(TENANT_ID,
      () -> {
        cleanResourceTables(jdbcTemplate);
      }
    );
  }

  @Test
  void incomingLdImportEvent_shouldBeHandledCorrectly() {
    // given
    var r1 = getSimpleInstanceModel(1L);
    var r2 = getSimpleInstanceModel(2L);
    var r3 = getSimpleInstanceModel(3L);
    var ldImportOutputEvent = getImportOutputEventProducerRecord(List.of(r1, r2, r3));

    // when
    eventKafkaTemplate.send(ldImportOutputEvent);

    // then
    awaitAndAssertResource(r1);
    awaitAndAssertResource(r2);
    awaitAndAssertResource(r3);
  }

  @SneakyThrows
  private ProducerRecord<String, String> getImportOutputEventProducerRecord(List<Resource> resources) {
    var topic = getTenantTopicName(LD_IMPORT_OUTPUT_TOPIC, TENANT_ID);
    var value = OBJECT_MAPPER.writeValueAsString(Map.of("resources", resources));
    var headers = new ArrayList<>(defaultKafkaHeaders());
    return new ProducerRecord(topic, 0, "1", value, headers);
  }

  private void awaitAndAssertResource(Resource given) {
    awaitAndAssert(() -> assertTrue(
      resourceRepository.existsById(given.getId())
    ));

    var found = tenantScopedExecutionService.execute(TENANT_ID,
      () -> resourceRepository.findByIdWithEdgesLoaded(given.getId())
    );

    assertThat(found).isPresent()
      .get().isEqualTo(resourceModelMapper.toEntity(given));
  }
}
