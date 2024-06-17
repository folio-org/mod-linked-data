package org.folio.linked.data.integration.kafka.sender.inventory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.linked.data.util.Constants.SEARCH_RESOURCE_NAME;
import static org.folio.search.domain.dto.ResourceEventType.CREATE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.folio.linked.data.integration.kafka.message.InstanceIngressEventMessage;
import org.folio.linked.data.integration.kafka.message.SearchIndexEventMessage;
import org.folio.linked.data.mapper.kafka.KafkaInventoryMessageMapper;
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
public class KafkaInventorySenderTest {

  @InjectMocks
  private KafkaInventorySenderImpl kafkaInventorySender;
  @Mock
  private KafkaInventoryMessageMapper kafkaInventoryMessageMapper;
  @Mock
  private FolioMessageProducer<InstanceIngressEventMessage> instanceIngressMessageProducer;

  @Test
  void sendInstanceCreated_shouldDoNothing_ifGivenResourceIsNotBeingMappedByMessageMapper() {
    // given
    var resource = new Resource().setId(123L);
    when(kafkaInventoryMessageMapper.toInstanceIngressPayload(resource)).thenReturn(Optional.empty());

    // when
    kafkaInventorySender.sendInstanceCreated(resource);

    // then
    verify(instanceIngressMessageProducer, never()).sendMessages(any());
  }

  @Test
  void sendInstanceCreated_shouldSendExpectedMessage_ifGivenResourceIsBeingMappedByMessageMapper() {
    // given
    var resource = new Resource().setId(123L);
    var payload = new InstanceIngressPayload().sourceRecordIdentifier(UUID.randomUUID().toString());
    when(kafkaInventoryMessageMapper.toInstanceIngressPayload(resource)).thenReturn(Optional.of(payload));

    // when
    kafkaInventorySender.sendInstanceCreated(resource);

    // then
    var messageCaptor = ArgumentCaptor.forClass(List.class);
    verify(instanceIngressMessageProducer).sendMessages(messageCaptor.capture());
    List<InstanceIngressEventMessage> messages = messageCaptor.getValue();
    assertThat(messages).hasSize(1);
    var message = messages.get(0);
    assertThat(message.getId()).isNotNull();
    assertThat(message.getEventPayload()).isEqualTo(payload);
    assertThat(message.getEventType()).isEqualTo(InstanceIngressEvent.EventTypeEnum.CREATE_INSTANCE);
  }
}
