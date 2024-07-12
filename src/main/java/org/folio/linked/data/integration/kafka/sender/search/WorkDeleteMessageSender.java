package org.folio.linked.data.integration.kafka.sender.search;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static org.folio.ld.dictionary.PredicateDictionary.INSTANTIATES;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.folio.linked.data.util.BibframeUtils.extractWork;
import static org.folio.linked.data.util.Constants.FOLIO_PROFILE;
import static org.folio.linked.data.util.Constants.SEARCH_RESOURCE_NAME;
import static org.folio.search.domain.dto.ResourceIndexEventType.DELETE;

import java.util.Collection;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.integration.ResourceModificationEventListener;
import org.folio.linked.data.integration.kafka.sender.DeleteMessageSender;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.model.entity.event.ResourceUpdatedEvent;
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
public class WorkDeleteMessageSender implements DeleteMessageSender {

  @Qualifier("bibliographicMessageProducer")
  private final FolioMessageProducer<ResourceIndexEvent> bibliographicMessageProducer;
  private final ResourceModificationEventListener eventListener;

  public WorkDeleteMessageSender(FolioMessageProducer<ResourceIndexEvent> bibliographicMessageProducer,
                                 @Lazy ResourceModificationEventListener eventListener) {
    this.bibliographicMessageProducer = bibliographicMessageProducer;
    this.eventListener = eventListener;
  }

  @Override
  public Collection<Resource> apply(Resource resource) {
    if (resource.isOfType(WORK)) {
      return List.of(resource);
    }
    if (resource.isOfType(INSTANCE)) {
      triggerParentWorkUpdate(resource);
    }
    return emptyList();
  }

  @Override
  public void accept(Resource resource) {
    ofNullable(resource.getId())
      .map(Object::toString)
      .map(this::getDeleteIndexEvent)
      .map(List::of)
      .ifPresent(bibliographicMessageProducer::sendMessages);
  }

  private void triggerParentWorkUpdate(Resource instance) {
    extractWork(instance)
      .ifPresent(work -> {
        log.info("Instance [id {}] deletion triggered parent Work [id {}] update", instance.getId(), work.getId());
        var newWork = new Resource(work);
        newWork.getIncomingEdges().remove(new ResourceEdge(instance, work, INSTANTIATES));
        eventListener.afterUpdate(new ResourceUpdatedEvent(work, newWork));
      });
  }

  private ResourceIndexEvent getDeleteIndexEvent(String id) {
    return new ResourceIndexEvent()
      .id(id)
      .type(DELETE)
      .resourceName(SEARCH_RESOURCE_NAME)
      ._new(new LinkedDataWork(id));
  }

}
