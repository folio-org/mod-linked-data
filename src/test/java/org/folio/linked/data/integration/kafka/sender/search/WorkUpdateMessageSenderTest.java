package org.folio.linked.data.integration.kafka.sender.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.PredicateDictionary.INSTANTIATES;
import static org.folio.ld.dictionary.ResourceTypeDictionary.FAMILY;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.folio.search.domain.dto.ResourceIndexEventType.UPDATE;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.folio.linked.data.mapper.kafka.search.BibliographicSearchMessageMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.model.entity.event.ResourceIndexedEvent;
import org.folio.search.domain.dto.LinkedDataWork;
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
class WorkUpdateMessageSenderTest {

  @InjectMocks
  private WorkUpdateMessageSender producer;
  @Mock
  private FolioMessageProducer<ResourceIndexEvent> resourceMessageProducer;
  @Mock
  private ApplicationEventPublisher eventPublisher;
  @Mock
  private BibliographicSearchMessageMapper bibliographicSearchMessageMapper;

  @Test
  void produce_shouldNotSendMessageAndIndexEvent_ifGivenResourceIsNotWorkOrInstance() {
    // given
    var resource = new Resource().addTypes(FAMILY);

    // when
    producer.produce(resource);

    // then
    verifyNoInteractions(eventPublisher, resourceMessageProducer);
  }

  @Test
  void produce_shouldSendUpdateMessageAndIndexEvent_ifNewWorkIsIndexable() {
    // given
    long id = 1L;
    var newResource = new Resource().setId(id).setLabel("new").addTypes(WORK);
    var index = new LinkedDataWork().id(String.valueOf(id));
    when(bibliographicSearchMessageMapper.toIndex(newResource, UPDATE)).thenReturn(Optional.of(index));

    // when
    producer.produce(newResource);

    // then
    var messageCaptor = ArgumentCaptor.forClass(List.class);
    verify(resourceMessageProducer).sendMessages(messageCaptor.capture());
    List<ResourceIndexEvent> messages = messageCaptor.getValue();

    assertThat(messages)
      .singleElement()
      .hasFieldOrProperty("id")
      .hasFieldOrPropertyWithValue("type", UPDATE)
      .hasFieldOrPropertyWithValue("resourceName", "linked-data-work")
      .hasFieldOrPropertyWithValue("_new", index);
    verify(eventPublisher).publishEvent(new ResourceIndexedEvent(id));
  }

  @Test
  void produce_shouldSendUpdateParentWorkAndIndexEvent_ifResourceIsInstance() {
    // given
    var newInstance = new Resource().setId(1L).setLabel("newInstance").addTypes(INSTANCE);
    var work = new Resource().setId(3L).setLabel("work").addTypes(WORK);
    newInstance.addOutgoingEdge(new ResourceEdge(newInstance, work, INSTANTIATES));
    var workIndex = new LinkedDataWork().id(String.valueOf(work.getId()));
    when(bibliographicSearchMessageMapper.toIndex(work, UPDATE)).thenReturn(Optional.of(workIndex));

    // when
    producer.produce(newInstance);

    // then
    var messageCaptor = ArgumentCaptor.forClass(List.class);
    verify(resourceMessageProducer).sendMessages(messageCaptor.capture());
    List<ResourceIndexEvent> messages = messageCaptor.getValue();

    assertThat(messages)
      .singleElement()
      .hasFieldOrProperty("id")
      .hasFieldOrPropertyWithValue("type", UPDATE)
      .hasFieldOrPropertyWithValue("resourceName", "linked-data-work")
      .hasFieldOrPropertyWithValue("_new", workIndex);
    verify(eventPublisher).publishEvent(new ResourceIndexedEvent(work.getId()));
  }
}
