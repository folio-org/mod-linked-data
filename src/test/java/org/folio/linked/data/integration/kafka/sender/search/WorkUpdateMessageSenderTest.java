package org.folio.linked.data.integration.kafka.sender.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.folio.search.domain.dto.ResourceIndexEventType.UPDATE;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.folio.linked.data.mapper.kafka.search.BibliographicSearchMessageMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.event.ResourceIndexedEvent;
import org.folio.search.domain.dto.BibframeLanguagesInner;
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
  private FolioMessageProducer<ResourceIndexEvent> resourceIndexEventMessageProducer;
  @Mock
  private ApplicationEventPublisher eventPublisher;
  @Mock
  private BibliographicSearchMessageMapper bibliographicSearchMessageMapper;
  @Mock
  private WorkCreateMessageSender createEventProducer;
  @Mock
  private WorkDeleteMessageSender deleteEventProducer;

  @Test
  void sendWorkUpdated_shouldSendUpdate_ifNewWorkIsIndexableAndKeepsSameId() {
    // given
    long id = 1L;
    var newResource = new Resource().setId(id).setLabel("new").addTypes(WORK);
    var oldResource = new Resource().setId(id).setLabel("old").addTypes(WORK);
    var indexNew = new LinkedDataWork().id(String.valueOf(id));
    var indexOld = new LinkedDataWork().id(String.valueOf(id)).addLanguagesItem(new BibframeLanguagesInner());
    when(bibliographicSearchMessageMapper.toIndex(newResource, UPDATE))
      .thenReturn(Optional.of(indexNew))
      .thenReturn(Optional.of(indexOld));

    // when
    producer.produce(oldResource, newResource);

    // then
    var messageCaptor = ArgumentCaptor.forClass(List.class);
    verify(resourceIndexEventMessageProducer)
      .sendMessages(messageCaptor.capture());
    List<ResourceIndexEvent> messages = messageCaptor.getValue();

    assertThat(messages)
      .singleElement()
      .hasFieldOrProperty("id")
      .hasFieldOrPropertyWithValue("type", UPDATE)
      .hasFieldOrPropertyWithValue("resourceName", "linked-data-work")
      .hasFieldOrPropertyWithValue("_new", indexNew)
      .hasFieldOrPropertyWithValue("old", indexOld);
    verify(eventPublisher)
      .publishEvent(new ResourceIndexedEvent(id));
  }

  @Test
  void sendWorkUpdated_shouldSendDeleteAndCreate_ifNewWorkHasNewIdAndBothResourcesAreIndexable() {
    // given
    Long newId = 1L;
    Long oldId = 2L;
    var newResource = new Resource().setId(newId).setLabel("new").addTypes(WORK);
    var oldResource = new Resource().setId(oldId).setLabel("old").addTypes(WORK);

    // when
    producer.produce(oldResource, newResource);

    // then
    verify(createEventProducer)
      .accept(newResource);
    verify(deleteEventProducer)
      .accept(oldResource);
    verifyNoInteractions(resourceIndexEventMessageProducer);
  }

  @Test
  void sendWorkUpdated_shouldSendDelete_ifNewWorkIsNotIndexableButOldIs() {
    // given
    Long newId = 1L;
    Long oldId = 1L;
    var newResource = new Resource().setId(newId).setLabel("new").addTypes(WORK);
    var oldResource = new Resource().setId(oldId).setLabel("old").addTypes(WORK);
    when(bibliographicSearchMessageMapper.toIndex(newResource, UPDATE))
      .thenReturn(Optional.empty());

    // when
    producer.produce(oldResource, newResource);

    // then
    verify(deleteEventProducer)
      .accept(oldResource);
    verifyNoInteractions(resourceIndexEventMessageProducer, createEventProducer);
  }
}
