package org.folio.linked.data.integration.kafka.listener.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.linked.data.test.TestUtil.TENANT_ID;
import static org.folio.linked.data.test.TestUtil.awaitAndAssert;
import static org.folio.linked.data.test.kafka.KafkaEventsTestDataFixture.getInventoryInstanceEventSampleProducerRecord;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.linked.data.repo.FolioMetadataRepository;
import org.folio.linked.data.repo.ResourceRepository;
import org.folio.linked.data.service.resource.ResourceGraphService;
import org.folio.linked.data.service.tenant.TenantScopedExecutionService;
import org.folio.linked.data.test.MonographTestUtil;
import org.folio.linked.data.test.kafka.KafkaSearchWorkIndexTopicListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;

@IntegrationTest
class InventoryInstanceEventHandlerIT {

  @Autowired
  private KafkaTemplate<String, String> eventKafkaTemplate;
  @Autowired
  private TenantScopedExecutionService tenantScopedExecutionService;
  @Autowired
  private KafkaSearchWorkIndexTopicListener kafkaSearchWorkIndexTopicListener;
  @Autowired
  private ResourceRepository resourceRepository;
  @Autowired
  private ResourceGraphService resourceGraphService;
  @Autowired
  private FolioMetadataRepository folioMetadataRepository;

  @BeforeEach
  public void clean() {
    tenantScopedExecutionService.execute(TENANT_ID,
      () -> {
        kafkaSearchWorkIndexTopicListener.getMessages().clear();
        folioMetadataRepository.deleteAll();
        resourceRepository.deleteAll();
      }
    );
  }

  @Test
  void shouldProcessInventoryInstanceEvent() {
    // given
    var resource = MonographTestUtil.getSampleInstanceResource();
    resourceGraphService.saveMergingGraph(resource);
    var eventProducerRecord = getInventoryInstanceEventSampleProducerRecord();

    // when
    eventKafkaTemplate.send(eventProducerRecord);

    //then
    awaitAndAssert(() ->
      assertThat(kafkaSearchWorkIndexTopicListener.getMessages())
        .singleElement()
        .satisfies(
          msg -> assertTrue(msg.contains("\"fromDiscovery\":true") && msg.contains("\"staff\":true")))
    );
    assertThat(folioMetadataRepository.findByInventoryId("2165ef4b-001f-46b3-a60e-52bcdeb3d5a1"))
      .get()
      .satisfies(metadata -> assertTrue(metadata.getStaffSuppress() && metadata.getSuppressFromDiscovery()));
  }
}
