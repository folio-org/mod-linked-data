package org.folio.linked.data.service.resource.events;

import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.event.ResourceEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ResourceEventsPublisherImpl implements ResourceEventsPublisher {
  private final ResourceEventsProviderImpl eventsProvider;
  private final ApplicationEventPublisher applicationEventPublisher;

  @Override
  public void publishEventsForCreate(Resource resourceToCreate) {
    publishEvents(eventsProvider.getEventsForCreate(resourceToCreate));
  }

  @Override
  public void publishEventsForUpdate(Resource resourceToCreate) {
    publishEvents(eventsProvider.getEventsForUpdate(resourceToCreate));
  }

  @Override
  public void publishEventsForUpdate(Resource oldResource, Resource resourceToCreate) {
    publishEvents(eventsProvider.getEventsForUpdate(oldResource, resourceToCreate));
  }

  @Override
  public void publishEventsForDelete(Resource resourceToDelete) {
    publishEvents(eventsProvider.getEventsForDelete(resourceToDelete));
  }

  private void publishEvents(Set<ResourceEvent> events) {
    events.forEach(applicationEventPublisher::publishEvent);
  }
}
