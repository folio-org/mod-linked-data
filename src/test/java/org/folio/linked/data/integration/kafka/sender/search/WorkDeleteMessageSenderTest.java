package org.folio.linked.data.integration.kafka.sender.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.PredicateDictionary.INSTANTIATES;
import static org.folio.ld.dictionary.ResourceTypeDictionary.FAMILY;
import static org.folio.search.domain.dto.ResourceIndexEventType.DELETE;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.HashSet;
import java.util.List;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.search.domain.dto.LinkedDataWork;
import org.folio.search.domain.dto.ResourceIndexEvent;
import org.folio.spring.testing.type.UnitTest;
import org.folio.spring.tools.kafka.FolioMessageProducer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class WorkDeleteMessageSenderTest {

  @InjectMocks
  private WorkDeleteMessageSender producer;
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
    verifyNoInteractions(resourceMessageProducer, workUpdateMessageSender);
  }

  @Test
  void produce_shouldSendWorkDeletedMessage_forWork() {
    // given
    var id = 1L;
    var work = new Resource().setId(id).addTypes(ResourceTypeDictionary.WORK);

    // when
    producer.produce(work);

    // then
    var messageCaptor = ArgumentCaptor.forClass(List.class);
    verify(resourceMessageProducer)
      .sendMessages(messageCaptor.capture());
    List<ResourceIndexEvent> messages = messageCaptor.getValue();
    var expectedIndex = new LinkedDataWork().id(String.valueOf(id));

    assertThat(messages)
      .singleElement()
      .hasFieldOrProperty("id")
      .hasFieldOrPropertyWithValue("type", DELETE)
      .hasFieldOrPropertyWithValue("resourceName", "linked-data-work")
      .hasFieldOrPropertyWithValue("_new", expectedIndex);
    verifyNoMoreInteractions(workUpdateMessageSender);
  }

  @Test
  void produce_shouldTriggerWorkUpdate_forInstanceWithWork() {
    // given
    var instance = new Resource().setId(1L).addTypes(ResourceTypeDictionary.INSTANCE);
    var work = new Resource().setId(2L).addTypes(ResourceTypeDictionary.WORK);
    var edge = new ResourceEdge(instance, work, INSTANTIATES);
    instance.getOutgoingEdges().add(edge);
    work.getIncomingEdges().add(edge);
    var expectedNewWork = new Resource(work);
    expectedNewWork.setIncomingEdges(new HashSet<>());

    // when
    producer.produce(instance);

    // then
    verify(resourceMessageProducer, never()).sendMessages(ArgumentMatchers.any());
    verify(workUpdateMessageSender).produce(work);
  }
}
