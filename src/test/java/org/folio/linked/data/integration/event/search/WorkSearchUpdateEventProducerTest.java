package org.folio.linked.data.integration.event.search;

import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.mockito.Mockito.verify;

import org.folio.linked.data.integration.kafka.sender.search.KafkaSearchSender;
import org.folio.linked.data.model.entity.Resource;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


@UnitTest
@ExtendWith(MockitoExtension.class)
class WorkSearchUpdateEventProducerTest {

  @InjectMocks
  private WorkSearchUpdateEventProducer workSearchUpdateEventProducer;
  @Mock
  private KafkaSearchSender kafkaSearchSender;

  @Test
  void afterUpdate_shouldCall_sendResourceUpdated() {
    //given
    var resourceNew = new Resource().setId(1L).addTypes(WORK);
    var resourceOld = new Resource().setId(1L).addTypes(WORK);

    //when
    workSearchUpdateEventProducer.produce(resourceOld, resourceNew);

    //then
    verify(kafkaSearchSender)
      .sendWorkUpdated(resourceNew, resourceOld);
  }
}
