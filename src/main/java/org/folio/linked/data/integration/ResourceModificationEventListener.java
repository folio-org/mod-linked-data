package org.folio.linked.data.integration;

import static java.lang.String.format;
import static org.folio.ld.dictionary.ResourceTypeDictionary.CONCEPT;
import static org.folio.ld.dictionary.ResourceTypeDictionary.FAMILY;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PERSON;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.folio.linked.data.util.BibframeUtils.extractInstances;
import static org.folio.linked.data.util.BibframeUtils.extractWork;
import static org.folio.linked.data.util.Constants.FOLIO_PROFILE;
import static org.folio.linked.data.util.Constants.NOT_INDEXED;

import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.integration.kafka.sender.inventory.KafkaInventorySender;
import org.folio.linked.data.integration.kafka.sender.search.KafkaSearchSender;
import org.folio.linked.data.model.entity.Resource;
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

  private static final Set<ResourceTypeDictionary> AUTHORITY_TYPES = Set.of(CONCEPT, PERSON, FAMILY);

  private final KafkaSearchSender kafkaSearchSender;
  private final KafkaInventorySender kafkaInventorySender;
  private final ResourceRepository resourceRepository;

  @TransactionalEventListener
  public void afterCreate(ResourceCreatedEvent resourceCreatedEvent) {
    log.info("ResourceCreatedEvent received [{}]", resourceCreatedEvent);
    var resource = resourceRepository.getReferenceById(resourceCreatedEvent.id());
    if (isAuthority(resource)) {
      sendToAuthoritySearch(resource);
    } else {
      sendToSearch(resource);
      sendToInventory(resource);
    }
  }

  @TransactionalEventListener
  public void afterUpdate(ResourceUpdatedEvent resourceUpdatedEvent) {
    log.info("ResourceUpdatedEvent received [{}]", resourceUpdatedEvent);
    kafkaSearchSender.sendResourceUpdated(resourceUpdatedEvent.newWork(), resourceUpdatedEvent.oldWork());
  }

  @TransactionalEventListener
  public void afterDelete(ResourceDeletedEvent resourceDeletedEvent) {
    log.info("ResourceDeletedEvent received [{}]", resourceDeletedEvent);
    kafkaSearchSender.sendResourceDeleted(resourceDeletedEvent.work());
  }

  @EventListener
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void afterIndex(ResourceIndexedEvent resourceIndexedEvent) {
    log.info("ResourceIndexedEvent received [{}]", resourceIndexedEvent);
    resourceRepository.updateIndexDate(resourceIndexedEvent.resourceId());
  }

  private void sendToSearch(Resource resource) {
    if (resource.isOfType(WORK)) {
      kafkaSearchSender.sendSingleResourceCreated(resource);
    } else {
      extractWork(resource)
        .map(Resource::getId)
        .map(resourceRepository::getReferenceById)
        .ifPresentOrElse(kafkaSearchSender::sendSingleResourceCreated,
          () -> log.warn(format(NOT_INDEXED, resource.getId(), "created")));
    }
  }

  private void sendToAuthoritySearch(Resource resource) {
    kafkaSearchSender.sendAuthorityCreated(resource);
  }

  private void sendToInventory(Resource resource) {
    extractInstances(resource).forEach(kafkaInventorySender::sendInstanceCreated);
  }

  private boolean isAuthority(Resource resource) {
    return AUTHORITY_TYPES.stream()
      .anyMatch(resource::isOfType);
  }
}
