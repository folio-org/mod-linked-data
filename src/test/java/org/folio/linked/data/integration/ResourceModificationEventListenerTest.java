package org.folio.linked.data.integration;

import static org.folio.linked.data.test.TestUtil.randomLong;
import static org.folio.search.domain.dto.ResourceEventType.CREATE;
import static org.folio.search.domain.dto.ResourceEventType.UPDATE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.folio.linked.data.mapper.resource.kafka.KafkaMessageMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.event.ResourceCreatedEvent;
import org.folio.linked.data.model.entity.event.ResourceDeletedEvent;
import org.folio.linked.data.model.entity.event.ResourceIndexedEvent;
import org.folio.linked.data.model.entity.event.ResourceUpdatedEvent;
import org.folio.linked.data.repo.ResourceRepository;
import org.folio.linked.data.service.KafkaSender;
import org.folio.search.domain.dto.BibframeIndex;
import org.folio.spring.test.type.UnitTest;
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
  private KafkaMessageMapper kafkaMessageMapper;
  @Mock
  private KafkaSender kafkaSender;
  @Mock
  private ResourceRepository resourceRepository;

  @Test
  void afterCreate_shouldSendResourceCreatedMessageToKafka() {
    //given
    var resource = new Resource().setResourceHash(1L);
    var bibframeIndex = new BibframeIndex(resource.getResourceHash().toString());
    when(kafkaMessageMapper.toIndex(resource, CREATE)).thenReturn(Optional.of(bibframeIndex));

    //when
    resourceModificationEventListener.afterCreate(new ResourceCreatedEvent(resource));

    //then
    verify(kafkaSender).sendResourceCreated(bibframeIndex, true);
  }

  @Test
  void afterCreate_shouldNotSendResourceCreatedMessageToKafka_whenNothingToIndex() {
    //given
    var resource = new Resource().setResourceHash(1L);
    when(kafkaMessageMapper.toIndex(resource, CREATE)).thenReturn(Optional.empty());

    //when
    resourceModificationEventListener.afterCreate(new ResourceCreatedEvent(resource));

    //then
    verify(kafkaSender, never()).sendResourceCreated(any(), eq(true));
  }

  @Test
  void afterUpdate_shouldSendResourceCreatedMessageToKafka() {
    //given
    var resourceNew = new Resource().setResourceHash(1L);
    var resourceOld = new Resource().setResourceHash(2L);
    var bibframeIndexNew = new BibframeIndex(resourceNew.getResourceHash().toString());
    when(kafkaMessageMapper.toIndex(resourceNew, UPDATE)).thenReturn(Optional.of(bibframeIndexNew));
    var bibframeIndexOld = new BibframeIndex(resourceOld.getResourceHash().toString());
    when(kafkaMessageMapper.toIndex(resourceOld, UPDATE)).thenReturn(Optional.of(bibframeIndexOld));

    //when
    resourceModificationEventListener.afterUpdate(new ResourceUpdatedEvent(resourceNew, resourceOld));

    //then
    verify(kafkaSender).sendResourceUpdated(bibframeIndexNew, bibframeIndexOld);
  }

  @Test
  void afterUpdate_shouldNotSendResourceCreatedMessageToKafka_whenNothingToIndexAndNoOldWork() {
    //given
    var resource = new Resource().setResourceHash(1L);
    when(kafkaMessageMapper.toIndex(resource, UPDATE)).thenReturn(Optional.empty());

    //when
    resourceModificationEventListener.afterUpdate(new ResourceUpdatedEvent(resource, null));

    //then
    verify(kafkaSender, never()).sendResourceUpdated(any(), any());
  }

  @Test
  void afterUpdate_shouldSendResourceDeletedMessageToKafka_whenNothingToIndexInNewWork() {
    //given
    var resource = new Resource().setResourceHash(1L);
    when(kafkaMessageMapper.toIndex(resource, UPDATE)).thenReturn(Optional.empty());

    //when
    resourceModificationEventListener.afterUpdate(new ResourceUpdatedEvent(resource, null));

    //then
    verify(kafkaSender).sendResourceDeleted(resource.getResourceHash());
    verify(kafkaSender, never()).sendResourceUpdated(any(), any());
  }

  @Test
  void afterDelete_shouldSendResourceDeletedMessageToKafka() {
    //given
    var resource = new Resource().setResourceHash(randomLong());
    var resourceDeletedEvent = new ResourceDeletedEvent(resource);
    when(kafkaMessageMapper.toDeleteIndexId(resource)).thenReturn(Optional.of(resource.getResourceHash()));

    //when
    resourceModificationEventListener.afterDelete(resourceDeletedEvent);

    //then
    verify(kafkaSender).sendResourceDeleted(resource.getResourceHash());
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
