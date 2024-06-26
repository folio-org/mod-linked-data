package org.folio.linked.data.integration.event.inventory;

import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.linked.data.model.entity.ResourceSource.LINKED_DATA;
import static org.folio.linked.data.model.entity.ResourceSource.MARC;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.folio.linked.data.integration.kafka.sender.inventory.KafkaInventorySender;
import org.folio.linked.data.model.entity.InstanceMetadata;
import org.folio.linked.data.model.entity.Resource;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class InventoryCreateInstanceEventProducerTest {
  @Mock
  private KafkaInventorySender kafkaInventorySender;
  @InjectMocks
  private InventoryCreateInstanceEventProducer producer;

  @Test
  void shouldCall_sendInstanceCreated_ifSourcedFromLinkedData() {
    // given
    var resource = new Resource()
      .setId(1L)
      .addTypes(INSTANCE);
    resource.setInstanceMetadata(new InstanceMetadata(resource).setSource(LINKED_DATA));

    // when
    producer.produce(resource);

    // then
    verify(kafkaInventorySender).sendInstanceCreated(resource);
  }

  @Test
  void shouldNotCall_sendInstanceCreated_ifNotSourcedFromLinkedData() {
    // given
    var resource = new Resource()
      .setId(1L)
      .addTypes(INSTANCE);
    resource.setInstanceMetadata(new InstanceMetadata(resource).setSource(MARC));

    // when
    producer.produce(resource);

    // then
    verifyNoInteractions(kafkaInventorySender);
  }
}
