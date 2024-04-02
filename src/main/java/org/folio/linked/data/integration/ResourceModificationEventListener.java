package org.folio.linked.data.integration;

import static org.folio.linked.data.util.Constants.FOLIO_PROFILE;
import static org.folio.search.domain.dto.ResourceEventType.CREATE;
import static org.folio.search.domain.dto.ResourceEventType.UPDATE;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.mapper.kafka.KafkaMessageMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.event.ResourceCreatedEvent;
import org.folio.linked.data.model.entity.event.ResourceDeletedEvent;
import org.folio.linked.data.model.entity.event.ResourceIndexedEvent;
import org.folio.linked.data.model.entity.event.ResourceUpdatedEvent;
import org.folio.linked.data.repo.ResourceRepository;
import org.folio.linked.data.service.KafkaSender;
import org.folio.search.domain.dto.BibframeIndex;
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
  public void afterUpdate(ResourceUpdatedEvent ruEvent) {
    log.info("ResourceUpdatedEvent received [{}]", ruEvent);
    kafkaMessageMapper.toIndex(ruEvent.newWork(), UPDATE).ifPresentOrElse(
      newWorkIndex -> indexUpdatedWork(ruEvent, newWorkIndex),
      () -> {
        sendDelete(ruEvent.newWork());
        log.info("Updated Work [{}] is not indexable anymore, sending DELETE event", ruEvent.newWork().getId());
      }
    );
  }

  @TransactionalEventListener
  public void afterDelete(ResourceDeletedEvent resourceDeletedEvent) {
    log.info("ResourceDeletedEvent received [{}]", resourceDeletedEvent);
    sendDelete(resourceDeletedEvent.work());
  }

  @EventListener
  @Transactional
  public void afterIndex(ResourceIndexedEvent resourceIndexedEvent) {
    log.info("ResourceIndexedEvent received [{}]", resourceIndexedEvent);
    resourceRepository.updateIndexDate(resourceIndexedEvent.workId());
  }

  private void indexUpdatedWork(ResourceUpdatedEvent resourceUpdatedEvent, BibframeIndex newWorkIndex) {
    if (resourceUpdatedEvent.isSameResourceUpdated()) {
      var oldWorkIndex = kafkaMessageMapper.toIndex(resourceUpdatedEvent.oldWork(), UPDATE).orElse(null);
      kafkaSender.sendResourceUpdated(newWorkIndex, oldWorkIndex);
      log.info("Updated Work [{}] has the same id as before update, sending UPDATE event", newWorkIndex.getId());
    } else {
      sendDelete(resourceUpdatedEvent.oldWork());
      kafkaSender.sendResourceCreated(newWorkIndex, true);
      log.info("Updated Work [{}] has another id than before update ({}), sending DELETE and CREATE events",
        newWorkIndex.getId(), resourceUpdatedEvent.oldWork().getId());
    }
  }

  private void sendDelete(Resource resource) {
    kafkaMessageMapper.toDeleteIndexId(resource)
      .ifPresent(kafkaSender::sendResourceDeleted);
  }

}
