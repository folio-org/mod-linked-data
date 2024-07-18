package org.folio.linked.data.integration.kafka.sender.inventory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.PredicateDictionary.INSTANTIATES;
import static org.folio.linked.data.model.entity.ResourceSource.LINKED_DATA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.mapper.kafka.inventory.InstanceIngressMessageMapper;
import org.folio.linked.data.model.entity.InstanceMetadata;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.search.domain.dto.InstanceIngressEvent;
import org.folio.search.domain.dto.InstanceIngressPayload;
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
    var notInstance = new Resource().setId(123L).addTypes(ResourceTypeDictionary.FAMILY);

    // when
    producer.produce(notInstance);

    // then
    verifyNoInteractions(instanceIngressMessageProducer);
  }

  @Test
  void produce_shouldSendExpectedMessage_ifGivenResourceIsInstanceLinkedDataSourced() {
    // given
    var instance = new Resource().setId(123L).addTypes(ResourceTypeDictionary.INSTANCE);
    var metadata = new InstanceMetadata(instance).setSource(LINKED_DATA).setInventoryId(UUID.randomUUID().toString());
    instance.setInstanceMetadata(metadata);

    var instanceIngressEvent = new InstanceIngressEvent().id(String.valueOf(instance.getId()))
      .eventPayload(new InstanceIngressPayload().sourceRecordIdentifier(metadata.getInventoryId()));
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
    var work = new Resource().setId(1L).addTypes(ResourceTypeDictionary.WORK);
    var instance1 = new Resource().setId(2L).addTypes(ResourceTypeDictionary.INSTANCE);
    var metadata1 = new InstanceMetadata(instance1).setSource(LINKED_DATA).setInventoryId(UUID.randomUUID().toString());
    instance1.setInstanceMetadata(metadata1);
    var instance2 = new Resource().setId(3L).addTypes(ResourceTypeDictionary.INSTANCE)
      .setInstanceMetadata(new InstanceMetadata(work).setSource(LINKED_DATA));
    var metadata2 = new InstanceMetadata(instance2).setSource(LINKED_DATA).setInventoryId(UUID.randomUUID().toString());
    instance2.setInstanceMetadata(metadata2);
    work.addIncomingEdge(new ResourceEdge(work, instance1, INSTANTIATES));
    work.addIncomingEdge(new ResourceEdge(work, instance2, INSTANTIATES));

    var ingressEvent1 = new InstanceIngressEvent().id(String.valueOf(instance1.getId()))
      .eventPayload(new InstanceIngressPayload().sourceRecordIdentifier(metadata1.getInventoryId()));
    when(instanceIngressMessageMapper.toInstanceIngressEvent(instance1)).thenReturn(ingressEvent1);
    var ingressEvent2 = new InstanceIngressEvent().id(String.valueOf(instance2.getId()))
      .eventPayload(new InstanceIngressPayload().sourceRecordIdentifier(metadata2.getInventoryId()));
    when(instanceIngressMessageMapper.toInstanceIngressEvent(instance2)).thenReturn(ingressEvent2);

    // when
    producer.produce(work);

    // then
    var messageCaptor = ArgumentCaptor.forClass(List.class);
    verify(instanceIngressMessageProducer, times(2)).sendMessages(messageCaptor.capture());
    var messagesLists = messageCaptor.getAllValues();
    assertThat(messagesLists).hasSize(2);
    assertThat(messagesLists.get(0)).singleElement().isEqualTo(ingressEvent1);
    assertThat(messagesLists.get(1)).singleElement().isEqualTo(ingressEvent2);
  }
}
