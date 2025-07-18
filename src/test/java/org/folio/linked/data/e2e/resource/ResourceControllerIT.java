package org.folio.linked.data.e2e.resource;

import static org.folio.linked.data.test.TestUtil.awaitAndAssert;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import lombok.SneakyThrows;
import org.folio.linked.data.domain.dto.InstanceIngressEvent;
import org.folio.linked.data.domain.dto.ResourceIndexEventType;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.linked.data.integration.kafka.sender.search.WorkCreateMessageSender;
import org.folio.linked.data.integration.kafka.sender.search.WorkDeleteMessageSender;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.test.kafka.KafkaInventoryTopicListener;
import org.folio.linked.data.test.kafka.KafkaProducerTestConfiguration;
import org.folio.linked.data.test.kafka.KafkaSearchWorkIndexTopicListener;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

@IntegrationTest
@SpringBootTest(classes = {KafkaProducerTestConfiguration.class})
class ResourceControllerIT extends ResourceControllerITBase {

  @Autowired
  private KafkaSearchWorkIndexTopicListener searchIndexTopicListener;
  @Autowired
  private KafkaInventoryTopicListener inventoryTopicListener;
  @MockitoSpyBean
  private WorkCreateMessageSender createEventProducer;
  @MockitoSpyBean
  private WorkDeleteMessageSender deleteEventProducer;

  @BeforeEach
  @Override
  public void beforeEach() {
    super.beforeEach();
    searchIndexTopicListener.getMessages().clear();
    inventoryTopicListener.getMessages().clear();
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
