package org.folio.linked.data.service.resource.events;

import static java.util.HashSet.newHashSet;
import static org.folio.ld.dictionary.ResourceTypeDictionary.HUB;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.model.entity.event.ResourceCreatedEvent;
import org.folio.linked.data.model.entity.event.ResourceDeletedEvent;
import org.folio.linked.data.model.entity.event.ResourceEvent;
import org.folio.linked.data.model.entity.event.ResourceReplacedEvent;
import org.folio.linked.data.model.entity.event.ResourceUpdatedEvent;
import org.folio.linked.data.repo.ResourceRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class ResourceEventsProviderImpl implements ResourceEventsProvider {

  private final ResourceRepository repository;

  @Override
  public Set<ResourceEvent> getEventsForCreate(Resource unsavedNewResource) {
    var newHubEvents = getNewHubEvents(unsavedNewResource);
    Set<ResourceEvent> result = newHashSet(newHubEvents.size() + 1);
    result.add(new ResourceCreatedEvent(unsavedNewResource));
    result.addAll(newHubEvents);
    return result;
  }

  @Override
  public Set<ResourceEvent> getEventsForUpdate(Resource unsavedNewResource) {
    return getEventsForUpdate(null, unsavedNewResource);
  }

  @Override
  public Set<ResourceEvent> getEventsForUpdate(Resource oldResource, Resource unsavedNewResource) {
    var newHubEvents = getNewHubEvents(unsavedNewResource);
    var resourceEvent = oldResource != null && Objects.equals(oldResource.getId(), unsavedNewResource.getId())
      ? new ResourceUpdatedEvent(unsavedNewResource)
      : new ResourceReplacedEvent(oldResource, unsavedNewResource.getId());

    Set<ResourceEvent> result = HashSet.newHashSet(newHubEvents.size() + 1);
    result.add(resourceEvent);
    result.addAll(newHubEvents);
    return result;
  }

  @Override
  public Set<ResourceEvent> getEventsForDelete(Resource resourceToDelete) {
    return Set.of(new ResourceDeletedEvent(resourceToDelete));
  }

  private List<ResourceCreatedEvent> getNewHubEvents(Resource unsavedNewResource) {
    return collectNewHubResourcesRecursive(unsavedNewResource)
      .stream()
      .map(ResourceCreatedEvent::new)
      .toList();
  }

  private Set<Resource> collectNewHubResourcesRecursive(Resource resource) {
    Set<Resource> result = new LinkedHashSet<>();
    if (!resource.isNew()) {
      return result;
    }
    if (resource.isOfType(HUB) && !repository.existsById(resource.getId())) {
      result.add(resource);
    }
    result.addAll(
      resource.getOutgoingEdges().stream()
        .map(ResourceEdge::getTarget)
        .filter(Resource::isNew)
        .flatMap(target -> collectNewHubResourcesRecursive(target).stream())
        .collect(Collectors.toSet())
    );
    return result;
  }
}
