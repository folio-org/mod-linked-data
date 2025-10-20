package org.folio.linked.data.service.resource.events;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.event.ResourceCreatedEvent;
import org.folio.linked.data.model.entity.event.ResourceDeletedEvent;
import org.folio.linked.data.model.entity.event.ResourceEvent;
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
  public void emitEventsForCreate(SaveGraphResult saveGraphResult) {
    emitResourceCreatedEvents(saveGraphResult.newResources());
  }

  @Override
  public void emitEventsForUpdate(SaveGraphResult saveGraphResult) {
    emitEventsForUpdate(null, saveGraphResult);
  }

  @Override
  public void emitEventsForUpdate(Resource oldResource, SaveGraphResult saveGraphResult) {
    var rootResource = saveGraphResult.rootResource();
    var rootResourceEvent = (oldResource == null || Objects.equals(oldResource.getId(), rootResource.getId()))
      ? new ResourceUpdatedEvent(rootResource)
      : new ResourceReplacedEvent(oldResource, rootResource.getId());

    emitEvent(rootResourceEvent);
    emitResourceCreatedEvents(
      saveGraphResult.newResources()
        .stream()
        .filter(r -> !Objects.equals(r.getId(), rootResource.getId()))
        .collect(Collectors.toSet())
    );
  }

  @Override
  public void emitEventForDelete(Resource deletedResource) {
    emitEvent(new ResourceDeletedEvent(deletedResource));
  }

  private void emitResourceCreatedEvents(Set<Resource> createdResources) {
    createdResources
      .stream()
      .map(ResourceCreatedEvent::new)
      .forEach(this::emitEvent);
  }

  private void emitEvent(ResourceEvent event) {
    applicationEventPublisher.publishEvent(event);
  }
}
