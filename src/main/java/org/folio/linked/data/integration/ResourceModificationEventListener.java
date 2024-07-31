package org.folio.linked.data.integration;

import static org.folio.linked.data.util.Constants.FOLIO_PROFILE;

import jakarta.persistence.EntityManager;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.integration.kafka.sender.CreateMessageSender;
import org.folio.linked.data.integration.kafka.sender.DeleteMessageSender;
import org.folio.linked.data.integration.kafka.sender.ReplaceMessageSender;
import org.folio.linked.data.integration.kafka.sender.UpdateMessageSender;
import org.folio.linked.data.model.entity.event.ResourceCreatedEvent;
import org.folio.linked.data.model.entity.event.ResourceDeletedEvent;
import org.folio.linked.data.model.entity.event.ResourceIndexedEvent;
import org.folio.linked.data.model.entity.event.ResourceReplacedEvent;
import org.folio.linked.data.model.entity.event.ResourceUpdatedEvent;
import org.folio.linked.data.repo.ResourceRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

@Log4j2
@Component
@Profile(FOLIO_PROFILE)
@RequiredArgsConstructor
public class ResourceModificationEventListener {

  private final EntityManager entityManager;
  private final ResourceRepository resourceRepository;
  private final Collection<CreateMessageSender> createMessageSenders;
  private final Collection<UpdateMessageSender> updateMessageSenders;
  private final Collection<ReplaceMessageSender> replaceMessageSenders;
  private final Collection<DeleteMessageSender> deleteMessageSenders;

  @TransactionalEventListener
  public void afterCreate(ResourceCreatedEvent resourceCreatedEvent) {
    log.info("ResourceCreatedEvent received [{}]", resourceCreatedEvent);
    entityManager.detach(resourceRepository.getReferenceById(resourceCreatedEvent.id()));
    var resource = resourceRepository.getReferenceById(resourceCreatedEvent.id());
    createMessageSenders.forEach(sender -> sender.produce(resource));
  }

  @TransactionalEventListener
  public void afterUpdate(ResourceUpdatedEvent resourceUpdatedEvent) {
    log.info("ResourceUpdatedEvent received [{}]", resourceUpdatedEvent);
    updateMessageSenders.forEach(sender -> sender.produce(resourceUpdatedEvent.resource()));
  }

  @TransactionalEventListener
  public void afterReplace(ResourceReplacedEvent resourceReplacedEvent) {
    log.info("ResourceReplacedEvent received [{}]", resourceReplacedEvent);
    replaceMessageSenders.forEach(
      sender -> sender.produce(resourceReplacedEvent.previous(), resourceReplacedEvent.current())
    );
  }

  @TransactionalEventListener
  public void afterDelete(ResourceDeletedEvent resourceDeletedEvent) {
    log.info("ResourceDeletedEvent received [{}]", resourceDeletedEvent);
    deleteMessageSenders.forEach(sender -> sender.produce(resourceDeletedEvent.resource()));
  }

  @EventListener
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void afterIndex(ResourceIndexedEvent resourceIndexedEvent) {
    log.info("ResourceIndexedEvent received [{}]", resourceIndexedEvent);
    resourceRepository.updateIndexDate(resourceIndexedEvent.resourceId());
  }
}
