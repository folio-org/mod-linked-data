package org.folio.linked.data.integration;

import static java.util.Optional.ofNullable;
import static org.folio.linked.data.util.Constants.FOLIO_PROFILE;
import static org.folio.search.domain.dto.ResourceEventType.CREATE;
import static org.folio.search.domain.dto.ResourceEventType.UPDATE;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.mapper.resource.kafka.KafkaMessageMapper;
import org.folio.linked.data.model.entity.event.ResourceCreatedEvent;
import org.folio.linked.data.model.entity.event.ResourceDeletedEvent;
import org.folio.linked.data.model.entity.event.ResourceIndexedEvent;
import org.folio.linked.data.model.entity.event.ResourceUpdatedEvent;
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
  private final KafkaMessageMapper kafkaMessageMapper;
  private final ResourceRepository resourceRepository;

  @TransactionalEventListener
  public void afterCreate(ResourceCreatedEvent resourceCreatedEvent) {
    log.info("ResourceCreatedEvent received [{}]", resourceCreatedEvent);
    kafkaMessageMapper.toIndex(resourceCreatedEvent.work(), CREATE)
      .ifPresent(bibframeIndex -> kafkaSender.sendResourceCreated(bibframeIndex, true));
  }

  @TransactionalEventListener
  public void afterUpdate(ResourceUpdatedEvent resourceUpdatedEvent) {
    log.info("ResourceUpdatedEvent received [{}]", resourceUpdatedEvent);
    kafkaMessageMapper.toIndex(resourceUpdatedEvent.newWork(), UPDATE)
      .ifPresentOrElse(newWorkIndex -> kafkaSender.sendResourceUpdated(newWorkIndex,
          kafkaMessageMapper.toIndex(resourceUpdatedEvent.oldWork(), UPDATE).orElse(null)),
        () -> ofNullable(resourceUpdatedEvent.oldWork()).ifPresent(oldWork -> {
          log.info("Updated Work resource [{}] contains no indexable values, removing it's index",
            oldWork.getResourceHash());
          kafkaSender.sendResourceDeleted(oldWork.getResourceHash());
        })
      );
  }

  @TransactionalEventListener
  public void afterDelete(ResourceDeletedEvent resourceDeletedEvent) {
    log.info("ResourceDeletedEvent received [{}]", resourceDeletedEvent);
    kafkaMessageMapper.toDeleteIndexId(resourceDeletedEvent.work())
      .ifPresent(kafkaSender::sendResourceDeleted);
  }

  @EventListener
  @Transactional
  public void afterIndex(ResourceIndexedEvent resourceIndexedEvent) {
    log.info("ResourceIndexedEvent received [{}]", resourceIndexedEvent);
    resourceRepository.updateIndexDate(resourceIndexedEvent.workId());
  }

}
