package org.folio.linked.data.integration.kafka.sender.inventory;

import static org.mockito.Mockito.verify;

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
  private InstanceUpdateMessageSender instanceUpdateMessageSender;

  @Test
  void produce_shouldCallInstanceUpdateMessageSenderProduce() {
    // given
    var resource = new Resource().setId(123L);

    // when
    producer.produce(new Resource(), resource);

    // then
    verify(instanceUpdateMessageSender).produce(resource);
  }

}
