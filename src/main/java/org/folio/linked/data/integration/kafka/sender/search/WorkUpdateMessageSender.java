package org.folio.linked.data.integration.kafka.sender.search;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.folio.linked.data.util.BibframeUtils.extractWork;
import static org.folio.linked.data.util.Constants.FOLIO_PROFILE;
import static org.folio.linked.data.util.Constants.SEARCH_RESOURCE_NAME;
import static org.folio.search.domain.dto.ResourceIndexEventType.UPDATE;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.integration.ResourceModificationEventListener;
import org.folio.linked.data.integration.kafka.sender.UpdateMessageSender;
import org.folio.linked.data.mapper.kafka.search.KafkaSearchMessageMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.event.ResourceCreatedEvent;
import org.folio.linked.data.model.entity.event.ResourceDeletedEvent;
import org.folio.linked.data.model.entity.event.ResourceIndexedEvent;
import org.folio.search.domain.dto.LinkedDataWork;
import org.folio.search.domain.dto.ResourceIndexEvent;
import org.folio.spring.tools.kafka.FolioMessageProducer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@Profile(FOLIO_PROFILE)
public class WorkUpdateMessageSender implements UpdateMessageSender {

  private static final String WRONG_UPDATE = "Invalid update operation for instance [id {}]: either new [{}] or old "
    + "work Id [{}] is missing. Such a situation is not expected to occur and should be investigated and fixed if it "
    + "does!";
  @Qualifier("bibliographicMessageProducer")
  private final FolioMessageProducer<ResourceIndexEvent> bibliographicMessageProducer;
  private final KafkaSearchMessageMapper<LinkedDataWork> searchBibliographicMessageMapper;
  private final ResourceModificationEventListener eventListener;

  public WorkUpdateMessageSender(FolioMessageProducer<ResourceIndexEvent> bibliographicMessageProducer,
                                 KafkaSearchMessageMapper<LinkedDataWork> searchBibliographicMessageMapper,
                                 @Lazy ResourceModificationEventListener eventListener) {
    this.bibliographicMessageProducer = bibliographicMessageProducer;
    this.searchBibliographicMessageMapper = searchBibliographicMessageMapper;
    this.eventListener = eventListener;
  }

  @Override
  public Collection<ResourcePair> apply(Resource oldResource, Resource newResource) {
    if (newResource.isOfType(WORK)) {
      return List.of(new ResourcePair(oldResource, newResource));
    }
    if (newResource.isOfType(INSTANCE)) {
      return triggerParentWorkUpdate(oldResource, newResource);
    }
    return emptyList();
  }

  @Override
  public void accept(Resource oldWork, Resource newWork) {
    if (isSameNotNullResource(newWork, oldWork) || isNull(oldWork)) {
      indexUpdatedWork(oldWork, newWork);
    } else {
      reCreate(oldWork, newWork);
    }
  }

  private void indexUpdatedWork(Resource oldWork, Resource newWork) {
    searchBibliographicMessageMapper.toIndex(newWork, UPDATE)
      .map(this::getUpdateIndexEvent)
      .map(indexEvent -> addOldWork(oldWork, indexEvent))
      .ifPresentOrElse(
        indexEvent -> {
          bibliographicMessageProducer.sendMessages(List.of(indexEvent));
          publishIndexEvent(newWork);
        },
        () -> deleteOld(oldWork)
      );
  }

  private List<ResourcePair> triggerParentWorkUpdate(Resource oldInstance, Resource newInstance) {
    var previousWork = extractWork(oldInstance).orElse(null);
    var currentWork = extractWork(newInstance).orElse(null);
    if (isSameNotNullResource(currentWork, previousWork)) {
      log.info("Instance [{}] update triggered parent Work [{}] update",
        newInstance.getId(), currentWork.getId());
      return singletonList(new ResourcePair(previousWork, currentWork));
    }
    logUnexpectedEvent(newInstance, previousWork, currentWork);
    return emptyList();
  }

  private void logUnexpectedEvent(Resource newInstance, Resource previousWork, Resource currentWork) {
    var currentWorkId = ofNullable(currentWork).map(Resource::getId).orElse(null);
    var previousWorkId = ofNullable(previousWork).map(Resource::getId).orElse(null);
    log.error(WRONG_UPDATE, newInstance.getId(), currentWorkId, previousWorkId);
  }

  private ResourceIndexEvent getUpdateIndexEvent(LinkedDataWork linkedDataWork) {
    return new ResourceIndexEvent()
      .id(linkedDataWork.getId())
      .type(UPDATE)
      .resourceName(SEARCH_RESOURCE_NAME)
      ._new(linkedDataWork);
  }

  private ResourceIndexEvent addOldWork(Resource oldWork, ResourceIndexEvent indexEvent) {
    return searchBibliographicMessageMapper.toIndex(oldWork, UPDATE)
      .map(indexEvent::old)
      .orElse(indexEvent);
  }

  private void reCreate(Resource oldResource, Resource newResource) {
    log.info("Updated Work [{}] has another id than before update ({}), sending DELETE and CREATE events",
      oldResource.getId(), newResource.getId());
    eventListener.afterDelete(new ResourceDeletedEvent(oldResource));
    eventListener.afterCreate(new ResourceCreatedEvent(newResource.getId()));
  }

  private void deleteOld(Resource oldResource) {
    ofNullable(oldResource)
      .ifPresent(r -> {
        log.info("Updated Work [{}] is not indexable anymore, sending DELETE event", r.getId());
        eventListener.afterDelete(new ResourceDeletedEvent(r));
      });
  }

  private boolean isSameNotNullResource(Resource newResource, Resource oldResource) {
    if (isNull(newResource)) {
      return false;
    }
    var oldResourceId = ofNullable(oldResource).map(Resource::getId).orElse(null);
    return Objects.equals(newResource.getId(), oldResourceId);
  }

  private void publishIndexEvent(Resource resource) {
    Optional.of(resource)
      .map(Resource::getId)
      .map(ResourceIndexedEvent::new)
      .ifPresent(eventListener::afterIndex);
  }

}
