package org.folio.linked.data.service.resource.events;

import static java.util.stream.Collectors.toSet;
import static org.folio.ld.dictionary.ResourceTypeDictionary.HUB;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.model.entity.event.ResourceCreatedEvent;
import org.folio.linked.data.model.entity.event.ResourceDeletedEvent;
import org.folio.linked.data.model.entity.event.ResourceEvent;
import org.folio.linked.data.model.entity.event.ResourceReplacedEvent;
import org.folio.linked.data.model.entity.event.ResourceUpdatedEvent;
import org.folio.linked.data.repo.ResourceRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ResourceEventsPublisherImpl implements ResourceEventsPublisher {
  private final ApplicationEventPublisher applicationEventPublisher;
  private final ResourceRepository resourceRepository;

  @Override
  public void publishEventsForCreate(Resource unsavedNewResource) {
    var events = getHubCreateEventsRecursive(unsavedNewResource);
    events.add(new ResourceCreatedEvent(unsavedNewResource));
    publishEvents(events);
  }

  @Override
  public void publishEventsForUpdate(Resource unsavedNewResource) {
    publishEventsForUpdate(null, unsavedNewResource);
  }

  @Override
  public void publishEventsForUpdate(Resource oldResource, Resource unsavedNewResource) {
    var events = getHubCreateEventsRecursive(unsavedNewResource);
    var resourceEvent = (oldResource == null || Objects.equals(oldResource.getId(), unsavedNewResource.getId()))
      ? new ResourceUpdatedEvent(unsavedNewResource)
      : new ResourceReplacedEvent(oldResource, unsavedNewResource.getId());
    events.add(resourceEvent);
    publishEvents(events);
  }

  @Override
  public void publishEventsForDelete(Resource resourceToDelete) {
    publishEvents(Set.of(new ResourceDeletedEvent(resourceToDelete)));
  }

  private void publishEvents(Set<ResourceEvent> events) {
    events.forEach(applicationEventPublisher::publishEvent);
  }

  private Set<ResourceEvent> getHubCreateEventsRecursive(Resource resource) {
    return getHubCreateEventsRecursive(resource, new LinkedHashSet<>());
  }

  private Set<ResourceEvent> getHubCreateEventsRecursive(Resource resource, Set<Long> visitedIds) {
    var result = new LinkedHashSet<ResourceEvent>();
    if (!visitedIds.add(resource.getId())) {
      return result;
    }
    if (resource.isOfType(HUB) && isNotIndexed(resource)) {
      result.add(new ResourceCreatedEvent(resource));
    }
    if (resource.isNew()) {
      result.addAll(
        resource.getOutgoingEdges().stream()
          .map(ResourceEdge::getTarget)
          .flatMap(target -> getHubCreateEventsRecursive(target, visitedIds).stream())
          .collect(toSet())
      );
    }
    return result;
  }

  private boolean isNotIndexed(Resource resource) {
    return resourceRepository.findById(resource.getId())
      .map(r -> r.getIndexDate() == null)
      .orElse(true);
  }
}
