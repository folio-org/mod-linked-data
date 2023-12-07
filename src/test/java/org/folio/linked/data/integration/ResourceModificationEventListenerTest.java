package org.folio.linked.data.integration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.folio.linked.data.mapper.ResourceMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.event.ResourceCreatedEvent;
import org.folio.linked.data.model.entity.event.ResourceDeletedEvent;
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
  private ResourceMapper resourceMapper;

  @Mock
  private KafkaSender kafkaSender;

  @Test
  void afterCreate_shouldSendResourceCreatedMessageToKafka() {
    //given
    var resource = new Resource().setResourceHash(1L);
    var bibframeIndex = new BibframeIndex(resource.getResourceHash().toString());

    when(resourceMapper.mapToIndex(resource)).thenReturn(Optional.of(bibframeIndex));

    //when
    resourceModificationEventListener.afterCreate(new ResourceCreatedEvent(resource));

    //then
    verify(kafkaSender).sendResourceCreated(bibframeIndex, true);
  }

  @Test
  void afterCreate_shouldNotSendResourceCreatedMessageToKafka_whenNothingToIndex() {
    //given
    var resource = new Resource().setResourceHash(1L);

    when(resourceMapper.mapToIndex(resource)).thenReturn(Optional.empty());

    //when
    resourceModificationEventListener.afterCreate(new ResourceCreatedEvent(resource));

    //then
    verify(kafkaSender, never()).sendResourceCreated(any(), eq(true));
  }

  @Test
  void afterDelete_shouldSendResourceDeletedMessageToKafka() {
    //given
    var resourceDeletedEvent = new ResourceDeletedEvent(1L);

    //when
    resourceModificationEventListener.afterDelete(resourceDeletedEvent);

    //then
    verify(kafkaSender).sendResourceDeleted(resourceDeletedEvent.id());
  }
}
