package org.folio.linked.data.integration;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.mapper.ResourceMapper;
import org.folio.linked.data.model.entity.event.ResourceCreatedEvent;
import org.folio.linked.data.model.entity.event.ResourceDeletedEvent;
import org.folio.linked.data.service.KafkaSender;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@RequiredArgsConstructor
@Component
public class SendKafkaMessageListener {

  private final ResourceMapper resourceMapper;
  private final KafkaSender kafkaSender;

  @TransactionalEventListener
  public void afterCreate(ResourceCreatedEvent resourceCreatedEvent) {
    resourceMapper.mapToIndex(resourceCreatedEvent.resource()).ifPresent(kafkaSender::sendResourceCreated);
  }

  @TransactionalEventListener
  public void afterDelete(ResourceDeletedEvent resourceDeletedEvent) {
    kafkaSender.sendResourceDeleted(resourceDeletedEvent.id());
  }
}
