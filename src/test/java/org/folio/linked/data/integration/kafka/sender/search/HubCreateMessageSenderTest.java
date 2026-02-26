package org.folio.linked.data.integration.kafka.sender.search;

import static java.lang.Long.parseLong;
import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.ResourceTypeDictionary.CONCEPT;
import static org.folio.ld.dictionary.ResourceTypeDictionary.FAMILY;
import static org.folio.ld.dictionary.ResourceTypeDictionary.HUB;
import static org.folio.ld.dictionary.ResourceTypeDictionary.LIGHT_RESOURCE;
import static org.folio.linked.data.domain.dto.ResourceIndexEventType.CREATE;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import org.folio.linked.data.domain.dto.ResourceIndexEvent;
import org.folio.linked.data.mapper.kafka.search.HubSearchMessageMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.event.ResourceIndexedEvent;
import org.folio.spring.testing.type.UnitTest;
import org.folio.spring.tools.kafka.FolioMessageProducer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@UnitTest
@ExtendWith(MockitoExtension.class)
class HubCreateMessageSenderTest {

  @InjectMocks
  private HubCreateMessageSender producer;
  @Mock
  private ApplicationEventPublisher eventPublisher;
  @Mock
  private HubSearchMessageMapper hubSearchMessageMapper;
  @Mock
  private FolioMessageProducer<ResourceIndexEvent> hubIndexMessageProducer;

  @Test
  void shouldSendMessageAndPublishIndexEvent_ifGivenResourceIsHubAndIndexable() {
    // given
    var resource = new Resource().addTypes(HUB).setIdAndRefreshEdges(123L);
    var expectedMessage = new ResourceIndexEvent().id(String.valueOf(resource.getId()));
    when(hubSearchMessageMapper.toIndex(resource, CREATE)).thenReturn(expectedMessage);

    // when
    producer.produce(resource);

    // then
    var messageCaptor = ArgumentCaptor.forClass(List.class);
    verify(hubIndexMessageProducer).sendMessages(messageCaptor.capture());
    assertThat(messageCaptor.getValue()).containsOnly(expectedMessage);
    assertThat(expectedMessage.getId()).isNotNull();
    var expectedIndexEvent = new ResourceIndexedEvent(parseLong(expectedMessage.getId()));
    verify(eventPublisher).publishEvent(expectedIndexEvent);
  }

  @Test
  void shouldNotSendMessageAndIndexEvent_ifGivenResourceIsNotHub() {
    // given
    var resource = new Resource().addTypes(FAMILY);

    // when
    producer.produce(resource);

    // then
    verifyNoInteractions(eventPublisher, hubIndexMessageProducer);
  }

  @Test
  void shouldNotSendMessageAndIndexEvent_ifGivenHubIsLight() {
    // given
    var lightHub = new Resource().addTypes(HUB, LIGHT_RESOURCE);

    // when
    producer.produce(lightHub);

    // then
    verifyNoInteractions(eventPublisher, hubIndexMessageProducer);
  }

  @Test
  void shouldNotSendMessageAndIndexEvent_ifGivenHubResourceHasAnotherType() {
    // given
    var resource = new Resource().addTypes(HUB, CONCEPT);

    // when
    producer.produce(resource);

    // then
    verifyNoInteractions(eventPublisher, hubIndexMessageProducer);
  }
}
