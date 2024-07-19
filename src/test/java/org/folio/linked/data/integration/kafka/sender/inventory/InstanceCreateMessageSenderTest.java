package org.folio.linked.data.integration.kafka.sender.inventory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.linked.data.model.entity.ResourceSource.LINKED_DATA;
import static org.folio.linked.data.model.entity.ResourceSource.MARC;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.mapper.kafka.inventory.InstanceIngressMessageMapper;
import org.folio.linked.data.model.entity.InstanceMetadata;
import org.folio.linked.data.model.entity.Resource;
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
class InstanceCreateMessageSenderTest {

  @InjectMocks
  private InstanceCreateMessageSender producer;
  @Mock
  private InstanceIngressMessageMapper instanceIngressMessageMapper;
  @Mock
  private FolioMessageProducer<InstanceIngressEvent> instanceIngressMessageProducer;

  @Test
  void produce_shouldDoNothing_ifGivenResourceIsNotInstance() {
    // given
    var resource = new Resource().setId(123L).addTypes(ResourceTypeDictionary.FAMILY);

    // when
    producer.produce(resource, null);

    // then
    verifyNoInteractions(instanceIngressMessageProducer);
  }

  @Test
  void produce_shouldDoNothing_ifGivenResourceIsInstanceMarcSourced() {
    // given
    var resource = new Resource().setId(123L).addTypes(ResourceTypeDictionary.INSTANCE);
    resource.setInstanceMetadata(new InstanceMetadata(resource).setSource(MARC));

    // when
    producer.produce(resource, null);

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
    producer.produce(instance, null);

    // then
    var messageCaptor = ArgumentCaptor.forClass(List.class);
    verify(instanceIngressMessageProducer).sendMessages(messageCaptor.capture());
    assertThat(messageCaptor.getValue()).singleElement().isEqualTo(instanceIngressEvent);
  }
}
