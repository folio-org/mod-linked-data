package org.folio.linked.data.integration.event.search;

import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.mockito.Mockito.verify;

import org.folio.linked.data.integration.kafka.sender.search.KafkaSearchSender;
import org.folio.linked.data.model.entity.Resource;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

class WorkSearchDeleteEventProducerTest {


  @InjectMocks
  private WorkSearchDeleteEventProducer workSearchDeleteEventProducer;
  @Mock
  private KafkaSearchSender kafkaSearchSender;

  @Test
  void afterDelete_shouldCall_sendResourceDeleted() {
    //given
    var resource = new Resource().setId(5L).addTypes(WORK);

    //when
    workSearchDeleteEventProducer.produce(resource);

    //then
    verify(kafkaSearchSender)
      .sendWorkCreated(resource);
  }
}
