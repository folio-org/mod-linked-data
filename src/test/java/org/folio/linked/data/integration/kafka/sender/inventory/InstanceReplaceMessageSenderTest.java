package org.folio.linked.data.integration.kafka.sender.inventory;

import static java.util.Collections.emptyList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.List;
import org.folio.linked.data.model.entity.Resource;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class InstanceReplaceMessageSenderTest {

  @InjectMocks
  private InstanceReplaceMessageSender producer;
  @Mock
  private InstanceUpdateMessageSender updateInstanceEventProducer;

  @Test
  void produce_shouldDoNothing_ifUpdateProducerAppliesNothing() {
    // given
    var resource = new Resource().setId(123L);
    doReturn(emptyList()).when(updateInstanceEventProducer).apply(resource);

    // when
    producer.produce(new Resource(), resource);

    // then
    verify(updateInstanceEventProducer, never()).accept(resource);
  }

  @Test
  void produce_shouldMakeUpdateProducerAccept_ifUpdateProducerApplies() {
    // given
    var resource = new Resource().setId(123L);
    doReturn(List.of(resource)).when(updateInstanceEventProducer).apply(resource);

    // when
    producer.produce(new Resource(), resource);

    // then
    verify(updateInstanceEventProducer).accept(resource);
  }
}
