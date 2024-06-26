package org.folio.linked.data.integration;

import static org.folio.linked.data.util.Constants.FOLIO_PROFILE;

import java.util.Collection;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.integration.event.CreateResourceEventProducer;
import org.folio.linked.data.integration.event.DeleteResourceEventProducer;
import org.folio.linked.data.integration.event.UpdateResourceEventProducer;
import org.folio.linked.data.model.entity.event.ResourceCreatedEvent;
import org.folio.linked.data.model.entity.event.ResourceDeletedEvent;
import org.folio.linked.data.model.entity.event.ResourceIndexedEvent;
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

  private final ResourceRepository resourceRepository;
  private final Collection<CreateResourceEventProducer> createResourceEventProducers;
  private final Collection<UpdateResourceEventProducer> updateResourceEventProducers;
  private final Collection<DeleteResourceEventProducer> deleteResourceEventProducers;

  @TransactionalEventListener
  public void afterCreate(ResourceCreatedEvent resourceCreatedEvent) {
    log.info("ResourceCreatedEvent received [{}]", resourceCreatedEvent);
    var resource = resourceRepository.getReferenceById(resourceCreatedEvent.id());
    createResourceEventProducers
      .forEach(event -> event.produce(resource));
  }

  @TransactionalEventListener
  public void afterUpdate(ResourceUpdatedEvent resourceUpdatedEvent) {
    log.info("ResourceUpdatedEvent received [{}]", resourceUpdatedEvent);
    var resource = resourceUpdatedEvent.oldWork();
    var newResource = resourceUpdatedEvent.newWork();
    updateResourceEventProducers
      .forEach(event -> event.produce(resource, newResource));
  }

  @TransactionalEventListener
  public void afterDelete(ResourceDeletedEvent resourceDeletedEvent) {
    log.info("ResourceDeletedEvent received [{}]", resourceDeletedEvent);
    var resource = resourceDeletedEvent.work();
    deleteResourceEventProducers
      .forEach(event -> event.produce(resource));
  }

  @EventListener
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void afterIndex(ResourceIndexedEvent resourceIndexedEvent) {
    log.info("ResourceIndexedEvent received [{}]", resourceIndexedEvent);
    resourceRepository.updateIndexDate(resourceIndexedEvent.resourceId());
  }
}
