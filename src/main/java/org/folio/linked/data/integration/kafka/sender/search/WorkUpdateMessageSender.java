package org.folio.linked.data.integration.kafka.sender.search;

import static org.folio.linked.data.util.Constants.FOLIO_PROFILE;
import static org.folio.linked.data.util.Constants.SEARCH_RESOURCE_NAME;
import static org.folio.search.domain.dto.ResourceIndexEventType.UPDATE;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.integration.kafka.sender.UpdateMessageSender;
import org.folio.linked.data.mapper.kafka.search.KafkaSearchMessageMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.event.ResourceIndexedEvent;
import org.folio.search.domain.dto.LinkedDataWork;
import org.folio.search.domain.dto.ResourceIndexEvent;
import org.folio.spring.tools.kafka.FolioMessageProducer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@Profile(FOLIO_PROFILE)
@RequiredArgsConstructor
public class WorkUpdateMessageSender implements UpdateMessageSender {

  @Qualifier("bibliographicMessageProducer")
  private final FolioMessageProducer<ResourceIndexEvent> bibliographicMessageProducer;
  private final KafkaSearchMessageMapper<LinkedDataWork> searchBibliographicMessageMapper;
  private final WorkCreateMessageSender createEventProducer;
  private final WorkDeleteMessageSender deleteEventProducer;
  private final ApplicationEventPublisher eventPublisher;


  @Override
  public Collection<ResourcePair> apply(Resource oldResource, Resource newResource) {
    return Stream.of(new ResourcePair(oldResource, newResource))
      .filter(resourcePair -> test(resourcePair.newResource()))
      .filter(resourcePair -> test(resourcePair.oldResource()))
      .toList();
  }

  //TODO refactoring to extract resources
  private boolean test(Resource resource) {
    return resource.isOfType(ResourceTypeDictionary.WORK);
  }

  @Override
  public void accept(Resource oldWork, Resource newWork) {
    if (isSameResource(oldWork, newWork)) {
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

  private ResourceIndexEvent getUpdateIndexEvent(LinkedDataWork linkedDataWork) {
    return new ResourceIndexEvent()
      .id(UUID.randomUUID().toString())
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
    deleteEventProducer.accept(oldResource);
    createEventProducer.accept(newResource);
  }

  private void deleteOld(Resource oldResource) {
    log.info("Updated Work [{}] is not indexable anymore, sending DELETE event", oldResource.getId());
    deleteEventProducer.accept(oldResource);
  }

  public static boolean isSameResource(Resource resource1, Resource resource2) {
    return Objects.equals(resource1.getId(), resource2.getId());
  }

  private void publishIndexEvent(Resource resource) {
    Optional.of(resource)
      .map(Resource::getId)
      .map(ResourceIndexedEvent::new)
      .ifPresent(eventPublisher::publishEvent);
  }
}
