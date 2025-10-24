package org.folio.linked.data.service.resource.events;

import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toSet;

import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.event.ResourceCreatedEvent;
import org.folio.linked.data.model.entity.event.ResourceDeletedEvent;
import org.folio.linked.data.model.entity.event.ResourceReplacedEvent;
import org.folio.linked.data.model.entity.event.ResourceUpdatedEvent;
import org.folio.linked.data.service.resource.graph.SaveGraphResult;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ResourceEventsPublisherImpl implements ResourceEventsPublisher {
  private final ApplicationEventPublisher applicationEventPublisher;

  @Override
  public void emitEventsForCreateAndUpdate(SaveGraphResult saveGraphResult, Resource oldResource) {
    var rootResource = saveGraphResult.rootResource();
    if (isNull(oldResource) && saveGraphResult.newResources().contains(rootResource)) {
      applicationEventPublisher.publishEvent(new ResourceCreatedEvent(rootResource));
    } else if (isNull(oldResource) || Objects.equals(oldResource.getId(), rootResource.getId())) {
      applicationEventPublisher.publishEvent(new ResourceUpdatedEvent(rootResource));
    } else {
      applicationEventPublisher.publishEvent(new ResourceReplacedEvent(oldResource, rootResource.getId()));
    }

    emitNewLinkedResourceEvents(
      saveGraphResult.newResources()
        .stream()
        .filter(r -> !Objects.equals(r.getId(), rootResource.getId()))
        .collect(toSet())
    );
  }

  @Override
  public void emitEventForDelete(Resource deletedResource) {
    applicationEventPublisher.publishEvent(new ResourceDeletedEvent(deletedResource));
  }

  private void emitNewLinkedResourceEvents(Set<Resource> createdResources) {
    createdResources
      .stream()
      .map(ResourceCreatedEvent::new)
      .forEach(applicationEventPublisher::publishEvent);
  }

}
