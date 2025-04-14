package org.folio.linked.data.integration;

import static org.folio.linked.data.util.Constants.STANDALONE_PROFILE;
import static org.folio.linked.data.util.ResourceUtils.getType;

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
@RequiredArgsConstructor
@Profile("!" + STANDALONE_PROFILE)
public class ResourceModificationEventListener {

  private final ResourceRepository resourceRepository;
  private final Collection<CreateMessageSender> createMessageSenders;
  private final Collection<UpdateMessageSender> updateMessageSenders;
  private final Collection<ReplaceMessageSender> replaceMessageSenders;
  private final Collection<DeleteMessageSender> deleteMessageSenders;

  @TransactionalEventListener
  public void afterCreate(ResourceCreatedEvent resourceCreatedEvent) {
    var resource = resourceCreatedEvent.resource();
    log.debug("ResourceCreatedEvent received [{}]", resourceCreatedEvent);
    log.info("Resource with id {} and type {} was created", resource.getId(), getType(resource));
    createMessageSenders.forEach(sender -> sender.produce(resource));
  }

  @TransactionalEventListener
  public void afterUpdate(ResourceUpdatedEvent resourceUpdatedEvent) {
    var resource = resourceUpdatedEvent.resource();
    log.debug("ResourceUpdatedEvent received [{}]", resourceUpdatedEvent);
    log.info("Resource with id {} and type {} was updated", resource.getId(), getType(resource));
    updateMessageSenders.forEach(sender -> sender.produce(resource));
  }

  @TransactionalEventListener
  public void afterReplace(ResourceReplacedEvent resourceReplacedEvent) {
    var previous = resourceReplacedEvent.previous();
    var currentResourceId = resourceReplacedEvent.currentResourceId();
    log.debug("ResourceReplacedEvent received [{}]", resourceReplacedEvent);
    log.info("Resource with id {} and type {} was replaced by resource with id {}",
      previous.getId(), getType(previous), currentResourceId);
    resourceRepository.findById(currentResourceId)
      .ifPresent(
        resource -> replaceMessageSenders.forEach(sender -> sender.produce(previous, resource))
      );
  }

  @TransactionalEventListener
  public void afterDelete(ResourceDeletedEvent resourceDeletedEvent) {
    var resource = resourceDeletedEvent.resource();
    log.debug("ResourceDeletedEvent received [{}]", resourceDeletedEvent);
    log.info("Resource with id {} and type {} was deleted", resource.getId(), getType(resource));
    deleteMessageSenders.forEach(sender -> sender.produce(resource));
  }

  @EventListener
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void afterIndex(ResourceIndexedEvent resourceIndexedEvent) {
    log.debug("ResourceIndexedEvent received [{}]", resourceIndexedEvent);
    log.info("Resource with id {} was indexed", resourceIndexedEvent.resourceId());
    resourceRepository.updateIndexDate(resourceIndexedEvent.resourceId());
  }
}
