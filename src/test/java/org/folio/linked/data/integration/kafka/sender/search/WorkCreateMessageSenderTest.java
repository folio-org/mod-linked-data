package org.folio.linked.data.integration.kafka.sender.search;

import static java.lang.Long.parseLong;
import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.PredicateDictionary.INSTANTIATES;
import static org.folio.ld.dictionary.ResourceTypeDictionary.FAMILY;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.folio.linked.data.test.TestUtil.randomLong;
import static org.folio.search.domain.dto.ResourceIndexEventType.CREATE;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import org.folio.linked.data.mapper.kafka.search.BibliographicSearchMessageMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.model.entity.event.ResourceIndexedEvent;
import org.folio.search.domain.dto.ResourceIndexEvent;
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
class WorkCreateMessageSenderTest {

  @InjectMocks
  private WorkCreateMessageSender producer;
  @Mock
  private ApplicationEventPublisher eventPublisher;
  @Mock
  private BibliographicSearchMessageMapper bibliographicSearchMessageMapper;
  @Mock
  private FolioMessageProducer<ResourceIndexEvent> resourceMessageProducer;
  @Mock
  private WorkUpdateMessageSender workUpdateMessageSender;

  @Test
  void produce_shouldNotSendMessageAndIndexEvent_ifGivenResourceIsNotWorkOrInstance() {
    // given
    var resource = new Resource().addTypes(FAMILY);

    // when
    producer.produce(resource);

    // then
    verifyNoInteractions(eventPublisher, resourceMessageProducer, workUpdateMessageSender);
  }

  @Test
  void produce_shouldSendMessageAndPublishIndexEvent_ifGivenResourceIsWorkAndIndexable() {
    // given
    var resource = new Resource().addTypes(WORK).setId(randomLong());
    var expectedMessage = new ResourceIndexEvent()
      .id(String.valueOf(resource.getId()));
    when(bibliographicSearchMessageMapper.toIndex(resource)).thenReturn(expectedMessage);

    // when
    producer.produce(resource);

    // then
    var messageCaptor = ArgumentCaptor.forClass(List.class);
    verify(resourceMessageProducer).sendMessages(messageCaptor.capture());
    assertThat(messageCaptor.getValue()).containsOnly(expectedMessage);
    assertThat(expectedMessage.getType()).isEqualTo(CREATE);
    var expectedIndexEvent = new ResourceIndexedEvent(parseLong(expectedMessage.getId()));
    verify(eventPublisher).publishEvent(expectedIndexEvent);
    verifyNoInteractions(workUpdateMessageSender);
  }

  @Test
  void produce_shouldTriggerWorkUpdate_ifGivenResourceIsInstanceWithWorkReference() {
    // given
    var instance = new Resource().addTypes(INSTANCE).setId(randomLong());
    var work = new Resource().addTypes(WORK).setId(randomLong());
    instance.addOutgoingEdge(new ResourceEdge(instance, work, INSTANTIATES));

    // when
    producer.produce(instance);

    // then
    verifyNoInteractions(resourceMessageProducer, eventPublisher);
    verify(workUpdateMessageSender).produce(work);
  }
}
