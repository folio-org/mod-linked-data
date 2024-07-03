package org.folio.linked.data.e2e;

import static org.folio.linked.data.test.TestUtil.FOLIO_TEST_PROFILE;
import static org.folio.linked.data.test.TestUtil.TENANT_ID;
import static org.folio.linked.data.test.TestUtil.awaitAndAssert;
import static org.folio.linked.data.util.Constants.FOLIO_PROFILE;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import lombok.SneakyThrows;
import org.folio.linked.data.integration.kafka.sender.search.WorkCreateMessageSender;
import org.folio.linked.data.integration.kafka.sender.search.WorkDeleteMessageSender;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.service.impl.tenant.TenantScopedExecutionService;
import org.folio.linked.data.test.ResourceTestService;
import org.folio.linked.data.test.kafka.KafkaInventoryTopicListener;
import org.folio.linked.data.test.kafka.KafkaSearchWorkIndexTopicListener;
import org.folio.search.domain.dto.InstanceIngressEvent;
import org.folio.search.domain.dto.ResourceIndexEventType;
import org.folio.spring.tools.kafka.KafkaAdminService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles({FOLIO_PROFILE, FOLIO_TEST_PROFILE})
public class ResourceControllerFolioIT extends ResourceControllerIT {

  @Autowired
  private KafkaSearchWorkIndexTopicListener searchIndexTopicListener;
  @Autowired
  private KafkaInventoryTopicListener inventoryTopicListener;
  @Autowired
  private TenantScopedExecutionService tenantScopedExecutionService;
  @Autowired
  private ResourceTestService resourceTestService;
  @SpyBean
  private WorkCreateMessageSender createEventProducer;
  @SpyBean
  private WorkDeleteMessageSender deleteEventProducer;

  @BeforeAll
  static void beforeAll(@Autowired KafkaAdminService kafkaAdminService) {
    kafkaAdminService.createTopics(TENANT_ID);
  }

  @BeforeEach
  public void beforeEach() {
    searchIndexTopicListener.getMessages().clear();
    inventoryTopicListener.getMessages().clear();
    tenantScopedExecutionService.execute(TENANT_ID, super::beforeEach);
  }

  @SneakyThrows
  @Override
  protected void checkSearchIndexMessage(Long id, ResourceIndexEventType eventType) {
    awaitAndAssert(() ->
      assertTrue(searchIndexTopicListener.getMessages().stream().anyMatch(m -> m.contains(id.toString())
        && m.contains(eventType.getValue())))
    );
  }

  @Override
  protected void checkInventoryMessage(Long id, InstanceIngressEvent.EventTypeEnum eventType) {
    awaitAndAssert(() ->
      assertTrue(inventoryTopicListener.getMessages().stream().anyMatch(m -> m.contains(id.toString())
        && m.contains(eventType.getValue())))
    );
  }

  @Override
  protected void checkIndexDate(String id) {
    assertNotNull(resourceTestService.getResourceById(id, 0).getIndexDate());
  }

  @Override
  protected void checkRelevantIndexMessagesDuringUpdate(Resource existedResource) {
    verify(deleteEventProducer, never()).accept(existedResource);
    verify(createEventProducer, never()).accept(any());
  }
}
