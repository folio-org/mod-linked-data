package org.folio.linked.data.integration.kafka.sender.inventory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.PredicateDictionary.INSTANTIATES;
import static org.folio.ld.dictionary.ResourceTypeDictionary.FAMILY;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.LIGHT_RESOURCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import org.folio.linked.data.domain.dto.InstanceIngressEvent;
import org.folio.linked.data.mapper.kafka.inventory.InstanceIngressMessageMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
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
class InstanceUpdateMessageSenderTest {

  @InjectMocks
  private InstanceUpdateMessageSender producer;
  @Mock
  private InstanceIngressMessageMapper instanceIngressMessageMapper;
  @Mock
  private FolioMessageProducer<InstanceIngressEvent> instanceIngressMessageProducer;

  @Test
  void produce_shouldDoNothing_ifGivenResourceIsNotInstance() {
    // given
    var notInstance = new Resource().setIdAndRefreshEdges(123L).addTypes(FAMILY);

    // when
    producer.produce(notInstance);

    // then
    verifyNoInteractions(instanceIngressMessageProducer);
  }

  @Test
  void produce_shouldDoNothing_ifGivenInstanceIsLightResource() {
    // given
    var lightInstance = new Resource().setIdAndRefreshEdges(123L).addTypes(INSTANCE, LIGHT_RESOURCE);

    // when
    producer.produce(lightInstance);

    // then
    verifyNoInteractions(instanceIngressMessageProducer);
  }


  @Test
  void produce_shouldDoNothing_ifGivenWorkIsLightResource() {
    // given
    var lightWork = new Resource().setIdAndRefreshEdges(123L).addTypes(WORK, LIGHT_RESOURCE);

    // when
    producer.produce(lightWork);

    // then
    verifyNoInteractions(instanceIngressMessageProducer);
  }

  @Test
  void produce_shouldSendExpectedMessage_ifGivenResourceIsInstance() {
    // given
    var instance = new Resource().setIdAndRefreshEdges(123L).addTypes(INSTANCE);
    var instanceIngressEvent = new InstanceIngressEvent().id(String.valueOf(instance.getId()));
    when(instanceIngressMessageMapper.toInstanceIngressEvent(instance)).thenReturn(instanceIngressEvent);

    // when
    producer.produce(instance);

    // then
    var messageCaptor = ArgumentCaptor.forClass(List.class);
    verify(instanceIngressMessageProducer).sendMessages(messageCaptor.capture());
    assertThat(messageCaptor.getValue()).singleElement().isEqualTo(instanceIngressEvent);
  }

  @Test
  void produce_shouldSendExpectedMessages_ifGivenResourceIsWorkWithInstances() {
    // given
    var work = new Resource().setIdAndRefreshEdges(1L).addTypes(WORK);
    var instance1 = new Resource().setIdAndRefreshEdges(2L).addTypes(INSTANCE);
    var instance2 = new Resource().setIdAndRefreshEdges(3L).addTypes(INSTANCE);
    var lightInstance = new Resource().setIdAndRefreshEdges(4L).addTypes(INSTANCE, LIGHT_RESOURCE);
    work.addIncomingEdge(new ResourceEdge(instance1, work, INSTANTIATES));
    work.addIncomingEdge(new ResourceEdge(instance2, work, INSTANTIATES));
    work.addIncomingEdge(new ResourceEdge(lightInstance, work, INSTANTIATES));

    var ingressEvent1 = new InstanceIngressEvent().id(String.valueOf(instance1.getId()));
    when(instanceIngressMessageMapper.toInstanceIngressEvent(instance1)).thenReturn(ingressEvent1);
    var ingressEvent2 = new InstanceIngressEvent().id(String.valueOf(instance2.getId()));
    when(instanceIngressMessageMapper.toInstanceIngressEvent(instance2)).thenReturn(ingressEvent2);

    // when
    producer.produce(work);

    // then
    var messageCaptor = ArgumentCaptor.forClass(List.class);
    verify(instanceIngressMessageProducer, times(2)).sendMessages(messageCaptor.capture());
    var messagesLists = messageCaptor.getAllValues();
    assertThat(messagesLists).hasSize(2);
    assertThat(messagesLists.getFirst()).singleElement().isEqualTo(ingressEvent1);
    assertThat(messagesLists.get(1)).singleElement().isEqualTo(ingressEvent2);
  }
}
