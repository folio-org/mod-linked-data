package org.folio.linked.data.integration;

import static org.folio.linked.data.util.Constants.FOLIO_PROFILE;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.mapper.ResourceMapper;
import org.folio.linked.data.mapper.resource.kafka.KafkaMessageMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.event.ResourceCreatedEvent;
import org.folio.linked.data.model.entity.event.ResourceDeletedEvent;
import org.folio.linked.data.model.entity.event.ResourceIndexedEvent;
import org.folio.linked.data.repo.ResourceRepository;
import org.folio.linked.data.service.KafkaSender;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

@Log4j2
@Component
@Profile(FOLIO_PROFILE)
@RequiredArgsConstructor
public class ResourceModificationEventListener {

  private final KafkaSender kafkaSender;
  private final ResourceMapper resourceMapper;
  private final ResourceRepository resourceRepository;
  private final KafkaMessageMapper kafkaMessageMapper;

  @TransactionalEventListener
  public void afterCreate(ResourceCreatedEvent resourceCreatedEvent) {
    log.info("ResourceCreatedEvent received [{}]", resourceCreatedEvent);
    resourceMapper.mapToIndex(resourceCreatedEvent.resource())
      .ifPresent(bibframeIndex -> kafkaSender.sendResourceCreated(bibframeIndex, true));
  }

  @TransactionalEventListener
  public void afterDelete(ResourceDeletedEvent resourceDeletedEvent) {
    log.info("ResourceDeletedEvent received [{}]", resourceDeletedEvent);
    kafkaMessageMapper.extractWork(resourceDeletedEvent.resource())
      .map(Resource::getResourceHash)
      .ifPresent(kafkaSender::sendResourceDeleted);
  }

  @EventListener
  @Transactional
  public void afterIndex(ResourceIndexedEvent resourceIndexedEvent) {
    log.info("ResourceIndexedEvent received [{}]", resourceIndexedEvent);
    resourceRepository.updateIndexDate(resourceIndexedEvent.id());
  }

}
