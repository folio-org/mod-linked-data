package org.folio.linked.data.integration.kafka.sender.inventory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.PredicateDictionary.INSTANTIATES;
import static org.folio.linked.data.model.entity.ResourceSource.LINKED_DATA;
import static org.folio.linked.data.model.entity.ResourceSource.MARC;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.mapper.kafka.inventory.KafkaInventoryMessageMapper;
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
class InventoryUpdateInstanceEventProducerTest {

  @InjectMocks
  private InventoryUpdateInstanceEventProducer producer;
  @Mock
  private KafkaInventoryMessageMapper kafkaInventoryMessageMapper;
  @Mock
  private FolioMessageProducer<InstanceIngressEvent> instanceIngressMessageProducer;

  @Test
  void produce_shouldDoNothing_ifGivenResourceIsNotInstance() {
    // given
    var instance = new Resource().setId(123L).addTypes(ResourceTypeDictionary.FAMILY);

    // when
    producer.produce(null, instance);

    // then
    verifyNoInteractions(instanceIngressMessageProducer);
  }

  @Test
  void produce_shouldDoNothing_ifGivenResourceIsInstanceMarcSourced() {
    // given
    var instance = new Resource().setId(123L).addTypes(ResourceTypeDictionary.INSTANCE);
    instance.setInstanceMetadata(new InstanceMetadata(instance).setSource(MARC));

    // when
    producer.produce(null, instance);

    // then
    verifyNoInteractions(instanceIngressMessageProducer);
  }

  @Test
  void produce_shouldDoNothing_ifGivenResourceIsNotBeingMappedByMessageMapper() {
    // given
    var instance = new Resource().setId(123L).addTypes(ResourceTypeDictionary.INSTANCE);
    instance.setInstanceMetadata(new InstanceMetadata(instance).setSource(LINKED_DATA));
    when(kafkaInventoryMessageMapper.toInstanceIngressEvent(instance)).thenReturn(Optional.empty());

    // when
    producer.produce(null, instance);

    // then
    verifyNoInteractions(instanceIngressMessageProducer);
  }

  @Test
  void produce_shouldSendExpectedMessage_ifGivenResourceIsInstanceLinkedDataSourcedAndMappedCorrectly() {
    // given
    var instance = new Resource().setId(123L).addTypes(ResourceTypeDictionary.INSTANCE);
    var metadata = new InstanceMetadata(instance).setSource(LINKED_DATA).setInventoryId(UUID.randomUUID().toString());
    instance.setInstanceMetadata(metadata);

    var instanceIngressEvent = new InstanceIngressEvent().id(String.valueOf(instance.getId()))
      .eventPayload(new InstanceIngressPayload().sourceRecordIdentifier(metadata.getInventoryId()));
    when(kafkaInventoryMessageMapper.toInstanceIngressEvent(instance)).thenReturn(Optional.of(instanceIngressEvent));

    // when
    producer.produce(null, instance);

    // then
    var messageCaptor = ArgumentCaptor.forClass(List.class);
    verify(instanceIngressMessageProducer).sendMessages(messageCaptor.capture());
    assertThat(messageCaptor.getValue()).singleElement().isEqualTo(instanceIngressEvent);
  }

  @Test
  void produce_shouldSendExpectedMessages_ifGivenResourceIsWorkWithInstancesLinkedDataSourcedAndMappedCorrectly() {
    // given
    var work = new Resource().setId(1L).addTypes(ResourceTypeDictionary.WORK);
    var instance1 = new Resource().setId(2L).addTypes(ResourceTypeDictionary.INSTANCE);
    var metadata1 = new InstanceMetadata(instance1).setSource(LINKED_DATA).setInventoryId(UUID.randomUUID().toString());
    instance1.setInstanceMetadata(metadata1);
    var instance2 = new Resource().setId(3L).addTypes(ResourceTypeDictionary.INSTANCE)
      .setInstanceMetadata(new InstanceMetadata(work).setSource(LINKED_DATA));
    var metadata2 = new InstanceMetadata(instance2).setSource(LINKED_DATA).setInventoryId(UUID.randomUUID().toString());
    instance2.setInstanceMetadata(metadata2);
    var instance3 = new Resource().setId(4L).addTypes(ResourceTypeDictionary.INSTANCE);
    work.addIncomingEdge(new ResourceEdge(work, instance1, INSTANTIATES));
    work.addIncomingEdge(new ResourceEdge(work, instance2, INSTANTIATES));
    work.addIncomingEdge(new ResourceEdge(work, instance3, INSTANTIATES));

    var ingressEvent1 = new InstanceIngressEvent().id(String.valueOf(instance1.getId()))
      .eventPayload(new InstanceIngressPayload().sourceRecordIdentifier(metadata1.getInventoryId()));
    when(kafkaInventoryMessageMapper.toInstanceIngressEvent(instance1)).thenReturn(Optional.of(ingressEvent1));
    var ingressEvent2 = new InstanceIngressEvent().id(String.valueOf(instance2.getId()))
      .eventPayload(new InstanceIngressPayload().sourceRecordIdentifier(metadata2.getInventoryId()));
    when(kafkaInventoryMessageMapper.toInstanceIngressEvent(instance2)).thenReturn(Optional.of(ingressEvent2));

    // when
    producer.produce(null, work);

    // then
    var messageCaptor = ArgumentCaptor.forClass(List.class);
    verify(instanceIngressMessageProducer, times(2)).sendMessages(messageCaptor.capture());
    var messagesLists = messageCaptor.getAllValues();
    assertThat(messagesLists).hasSize(2);
    assertThat(messagesLists.get(0)).singleElement().isEqualTo(ingressEvent1);
    assertThat(messagesLists.get(1)).singleElement().isEqualTo(ingressEvent2);
  }
}
