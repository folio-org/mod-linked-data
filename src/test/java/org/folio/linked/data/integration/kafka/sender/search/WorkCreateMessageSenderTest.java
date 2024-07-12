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
import java.util.Optional;
import org.folio.linked.data.integration.ResourceModificationEventListener;
import org.folio.linked.data.mapper.kafka.search.BibliographicSearchMessageMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.model.entity.event.ResourceIndexedEvent;
import org.folio.linked.data.model.entity.event.ResourceUpdatedEvent;
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
class WorkCreateMessageSenderTest {

  @InjectMocks
  private WorkCreateMessageSender producer;
  @Mock
  private ResourceModificationEventListener eventListener;
  @Mock
  private BibliographicSearchMessageMapper bibliographicSearchMessageMapper;
  @Mock
  private FolioMessageProducer<ResourceIndexEvent> resourceIndexEventMessageProducer;

  @Test
  void produce_shouldNotSendMessageAndIndexEvent_ifGivenResourceIsNotWorkOrInstance() {
    // given
    var resource = new Resource().addTypes(FAMILY);

    // when
    producer.produce(resource);

    // then
    verifyNoInteractions(eventListener, resourceIndexEventMessageProducer);
  }

  @Test
  void produce_shouldNotSendMessageAndIndexEvent_ifGivenResourceIsWorkButNotIndexable() {
    // given
    var resource = new Resource().addTypes(WORK);
    when(bibliographicSearchMessageMapper.toIndex(resource, CREATE))
      .thenReturn(Optional.empty());

    // when
    producer.produce(resource);

    // then
    verifyNoInteractions(eventListener, resourceIndexEventMessageProducer);
  }

  @Test
  void produce_shouldSendMessageAndPublishIndexEvent_ifGivenResourceIsWorkAndIndexable() {
    // given
    var resource = new Resource().addTypes(WORK).setId(randomLong());
    var index = new LinkedDataWork().id(String.valueOf(resource.getId()));
    when(bibliographicSearchMessageMapper.toIndex(resource, CREATE))
      .thenReturn(Optional.of(index));

    // when
    producer.produce(resource);

    // then
    var messageCaptor = ArgumentCaptor.forClass(List.class);
    verify(resourceIndexEventMessageProducer)
      .sendMessages(messageCaptor.capture());
    List<ResourceIndexEvent> messages = messageCaptor.getValue();
    var expectedIndexEvent = new ResourceIndexedEvent(parseLong(index.getId()));

    assertThat(messages)
      .singleElement()
      .hasFieldOrProperty("id")
      .hasFieldOrPropertyWithValue("type", CREATE)
      .hasFieldOrPropertyWithValue("resourceName", "linked-data-work")
      .hasFieldOrPropertyWithValue("_new", index);
    verify(eventListener)
      .afterIndex(expectedIndexEvent);
  }

  @Test
  void produce_shouldPublishWorkUpdateEvent_ifGivenResourceIsInstanceWithWorkReference() {
    // given
    var instance = new Resource().addTypes(INSTANCE).setId(randomLong());
    var work = new Resource().addTypes(WORK).setId(randomLong());
    instance.addOutgoingEdge(new ResourceEdge(instance, work, INSTANTIATES));

    // when
    producer.produce(instance);

    // then
    verifyNoInteractions(resourceIndexEventMessageProducer);
    var resourceUpdatedEventCaptor = ArgumentCaptor.forClass(ResourceUpdatedEvent.class);
    verify(eventListener).afterUpdate(resourceUpdatedEventCaptor.capture());
    var resourceUpdatedEvent = resourceUpdatedEventCaptor.getValue();
    assertThat(resourceUpdatedEvent.newResource()).isEqualTo(work);
    assertThat(resourceUpdatedEvent.oldResource()).isNull();
  }
}
