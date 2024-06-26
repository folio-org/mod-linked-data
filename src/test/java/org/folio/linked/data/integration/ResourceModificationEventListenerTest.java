package org.folio.linked.data.integration;

import static org.mockito.Mockito.verify;

import org.folio.linked.data.model.entity.event.ResourceIndexedEvent;
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
  private ResourceRepository resourceRepository;

  @Test
  void afterIndex_shouldSendResourceIndexedMessageToKafka() {
    //given
    var resourceIndexedEvent = new ResourceIndexedEvent(1L);

    //when
    resourceModificationEventListener.afterIndex(resourceIndexedEvent);

    //then
    verify(resourceRepository).updateIndexDate(resourceIndexedEvent.resourceId());
  }

}
