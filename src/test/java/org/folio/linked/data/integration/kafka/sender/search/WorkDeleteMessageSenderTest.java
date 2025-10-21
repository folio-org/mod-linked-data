package org.folio.linked.data.integration.kafka.sender.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.PredicateDictionary.INSTANTIATES;
import static org.folio.ld.dictionary.ResourceTypeDictionary.FAMILY;
import static org.folio.linked.data.domain.dto.ResourceIndexEventType.DELETE;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.HashSet;
import java.util.List;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.domain.dto.ResourceIndexEvent;
import org.folio.linked.data.mapper.kafka.search.WorkSearchMessageMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
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
  private WorkSearchMessageMapper workSearchMessageMapper;
  @Mock
  private WorkUpdateMessageSender workUpdateMessageSender;

  @Test
  void produce_shouldNotSendMessageAndIndexEvent_ifGivenResourceIsNotWorkOrInstance() {
    // given
    var resource = new Resource().addTypes(FAMILY);

    // when
    producer.produce(resource);

    // then
    verifyNoInteractions(resourceMessageProducer, workSearchMessageMapper, workUpdateMessageSender);
  }

  @Test
  void produce_shouldSendWorkDeletedMessage_forWork() {
    // given
    var id = 1L;
    var work = new Resource().setIdAndRefreshEdges(id).addTypes(ResourceTypeDictionary.WORK);
    var expectedMessage = new ResourceIndexEvent().id(String.valueOf(id));
    doReturn(expectedMessage).when(workSearchMessageMapper).toIndex(work);

    // when
    producer.produce(work);

    // then
    var messageCaptor = ArgumentCaptor.forClass(List.class);
    verify(resourceMessageProducer).sendMessages(messageCaptor.capture());
    assertThat(messageCaptor.getValue()).containsOnly(expectedMessage);
    assertThat(expectedMessage.getType()).isEqualTo(DELETE);
    verifyNoMoreInteractions(workUpdateMessageSender);
  }

  @Test
  void produce_shouldTriggerWorkUpdate_forInstanceWithWork() {
    // given
    var instance = new Resource().setIdAndRefreshEdges(1L).addTypes(ResourceTypeDictionary.INSTANCE);
    var work = new Resource().setIdAndRefreshEdges(2L).addTypes(ResourceTypeDictionary.WORK);
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
