package org.folio.linked.data.integration;

import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.folio.linked.data.test.TestUtil.randomLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.folio.linked.data.integration.kafka.sender.inventory.KafkaInventorySender;
import org.folio.linked.data.integration.kafka.sender.search.KafkaSearchSender;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.event.ResourceCreatedEvent;
import org.folio.linked.data.model.entity.event.ResourceDeletedEvent;
import org.folio.linked.data.model.entity.event.ResourceIndexedEvent;
import org.folio.linked.data.model.entity.event.ResourceUpdatedEvent;
import org.folio.linked.data.repo.ResourceRepository;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class ResourceModificationEventListenerTest {

  @InjectMocks
  private ResourceModificationEventListener resourceModificationEventListener;
  @Mock
  private KafkaSearchSender kafkaSearchSender;
  @Mock
  private KafkaInventorySender kafkaInventorySender;
  @Mock
  private ResourceRepository resourceRepository;

  @Test
  void afterCreate_shouldCall_sendSingleResourceCreated() {
    //given
    var resource = new Resource().setId(1L).addTypes(WORK);
    when(resourceRepository.getReferenceById(1L)).thenReturn(resource);

    //when
    resourceModificationEventListener.afterCreate(new ResourceCreatedEvent(resource.getId()));

    //then
    verify(kafkaSearchSender).sendSingleResourceCreated(resource);
  }

  @Test
  void afterUpdate_shouldCall_sendResourceUpdated() {
    //given
    var resourceNew = new Resource().setId(1L);
    var resourceOld = new Resource().setId(1L);

    //when
    resourceModificationEventListener.afterUpdate(new ResourceUpdatedEvent(resourceNew, resourceOld));

    //then
    verify(kafkaSearchSender).sendResourceUpdated(resourceNew, resourceOld);
  }

  @Test
  void afterDelete_shouldCall_sendResourceDeleted() {
    //given
    var resource = new Resource().setId(randomLong());

    //when
    resourceModificationEventListener.afterDelete(new ResourceDeletedEvent(resource));

    //then
    verify(kafkaSearchSender).sendResourceDeleted(resource);
  }

  @Test
  void afterIndex_shouldSendResourceIndexedMessageToKafka() {
    //given
    var resourceIndexedEvent = new ResourceIndexedEvent(1L);

    //when
    resourceModificationEventListener.afterIndex(resourceIndexedEvent);

    //then
    verify(resourceRepository).updateIndexDate(resourceIndexedEvent.workId());
  }

}
