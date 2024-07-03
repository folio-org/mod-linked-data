package org.folio.linked.data.integration.event.search;

import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.mockito.Mockito.verify;

import org.folio.linked.data.integration.kafka.sender.search.KafkaSearchSender;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.repo.ResourceRepository;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class WorkSearchCreateEventProducerTest {

  @InjectMocks
  private WorkSearchCreateEventProducer workSearchCreateEventProducer;
  @Mock
  private ResourceRepository resourceRepository;
  @Mock
  private KafkaSearchSender kafkaSearchSender;

  @Test
  void afterCreate_shouldCall_sendSingleResourceCreated() {
    //given
    var resource = new Resource().setId(1L).addTypes(WORK);

    //when
    workSearchCreateEventProducer.produce(resource);

    //then
    verify(kafkaSearchSender)
      .sendWorkCreated(resource);
  }
}
