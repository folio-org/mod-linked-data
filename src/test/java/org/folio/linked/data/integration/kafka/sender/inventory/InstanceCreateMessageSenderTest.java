package org.folio.linked.data.integration.kafka.sender.inventory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.ResourceTypeDictionary.FAMILY;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.LIGHT_RESOURCE;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import org.folio.linked.data.domain.dto.InstanceIngressEvent;
import org.folio.linked.data.mapper.kafka.inventory.InstanceIngressMessageMapper;
import org.folio.linked.data.model.entity.Resource;
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
    var resource = new Resource().setIdAndRefreshEdges(123L).addTypes(FAMILY);

    // when
    producer.produce(resource);

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
  void produce_shouldSendExpectedMessage() {
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
}
