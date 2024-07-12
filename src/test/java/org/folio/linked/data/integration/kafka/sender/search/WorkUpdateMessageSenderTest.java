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
import org.folio.linked.data.integration.ResourceModificationEventListener;
import org.folio.linked.data.mapper.kafka.search.BibliographicSearchMessageMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.model.entity.event.ResourceCreatedEvent;
import org.folio.linked.data.model.entity.event.ResourceDeletedEvent;
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

@UnitTest
@ExtendWith(MockitoExtension.class)
class WorkUpdateMessageSenderTest {

  @InjectMocks
  private WorkUpdateMessageSender producer;
  @Mock
  private FolioMessageProducer<ResourceIndexEvent> resourceIndexEventMessageProducer;
  @Mock
  private ResourceModificationEventListener eventListener;
  @Mock
  private BibliographicSearchMessageMapper bibliographicSearchMessageMapper;

  @Test
  void produce_shouldNotSendMessageAndIndexEvent_ifGivenResourceIsNotWorkOrInstance() {
    // given
    var resource = new Resource().addTypes(FAMILY);

    // when
    producer.produce(null, resource);

    // then
    verifyNoInteractions(eventListener, resourceIndexEventMessageProducer);
  }

  @Test
  void produce_shouldSendUpdateMessageAndIndexEvent_ifNewWorkIsIndexableAndKeepsSameId() {
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
    verify(eventListener)
      .afterIndex(new ResourceIndexedEvent(id));
  }

  @Test
  void produce_shouldSendDeleteAndCreate_ifNewWorkHasNewIdAndBothResourcesAreIndexable() {
    // given
    Long newId = 1L;
    Long oldId = 2L;
    var newResource = new Resource().setId(newId).setLabel("new").addTypes(WORK);
    var oldResource = new Resource().setId(oldId).setLabel("old").addTypes(WORK);

    // when
    producer.produce(oldResource, newResource);

    // then
    verify(eventListener).afterDelete(new ResourceDeletedEvent(oldResource));
    verify(eventListener).afterCreate(new ResourceCreatedEvent(newResource.getId()));
    verifyNoInteractions(resourceIndexEventMessageProducer);
  }

  @Test
  void produce_shouldSendDeleteAndIndexEvent_ifNewWorkIsNotIndexableButOldIs() {
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
    verify(eventListener).afterDelete(new ResourceDeletedEvent(oldResource));
    verifyNoInteractions(resourceIndexEventMessageProducer);
  }

  @Test
  void produce_shouldSendUpdateParentWorkAndIndexEvent_ifResourceIsInstance() {
    // given
    var newInstance = new Resource().setId(1L).setLabel("newInstance").addTypes(INSTANCE);
    var oldInstance = new Resource().setId(2L).setLabel("oldInstance").addTypes(INSTANCE);
    var work = new Resource().setId(3L).setLabel("work").addTypes(WORK);
    newInstance.addOutgoingEdge(new ResourceEdge(newInstance, work, INSTANTIATES));
    oldInstance.addOutgoingEdge(new ResourceEdge(oldInstance, work, INSTANTIATES));
    var workIndex = new LinkedDataWork().id(String.valueOf(work.getId()));
    when(bibliographicSearchMessageMapper.toIndex(work, UPDATE)).thenReturn(Optional.of(workIndex));

    // when
    producer.produce(oldInstance, newInstance);

    // then
    var messageCaptor = ArgumentCaptor.forClass(List.class);
    verify(resourceIndexEventMessageProducer).sendMessages(messageCaptor.capture());
    List<ResourceIndexEvent> messages = messageCaptor.getValue();

    assertThat(messages)
      .singleElement()
      .hasFieldOrProperty("id")
      .hasFieldOrPropertyWithValue("type", UPDATE)
      .hasFieldOrPropertyWithValue("resourceName", "linked-data-work")
      .hasFieldOrPropertyWithValue("_new", workIndex)
      .hasFieldOrPropertyWithValue("old", workIndex);
    verify(eventListener).afterIndex(new ResourceIndexedEvent(work.getId()));
  }
}
