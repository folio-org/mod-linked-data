package org.folio.linked.data.service.resource.events;

import static org.folio.ld.dictionary.ResourceTypeDictionary.HUB;

import java.util.LinkedHashSet;
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
    var result = collectHubEventsRecursive(unsavedNewResource);
    result.add(new ResourceCreatedEvent(unsavedNewResource));
    return result;
  }

  @Override
  public Set<ResourceEvent> getEventsForUpdate(Resource unsavedNewResource) {
    return getEventsForUpdate(null, unsavedNewResource);
  }

  @Override
  public Set<ResourceEvent> getEventsForUpdate(Resource oldResource, Resource unsavedNewResource) {
    var result = collectHubEventsRecursive(unsavedNewResource);
    var resourceEvent = oldResource != null && Objects.equals(oldResource.getId(), unsavedNewResource.getId())
      ? new ResourceUpdatedEvent(unsavedNewResource)
      : new ResourceReplacedEvent(oldResource, unsavedNewResource.getId());
    result.add(resourceEvent);
    return result;
  }

  @Override
  public Set<ResourceEvent> getEventsForDelete(Resource resourceToDelete) {
    return Set.of(new ResourceDeletedEvent(resourceToDelete));
  }

  private Set<ResourceEvent> collectHubEventsRecursive(Resource resource) {
    var result = new LinkedHashSet<ResourceEvent>();
    if (resource.isOfType(HUB) && isNotIndexed(resource)) {
      result.add(new ResourceCreatedEvent(resource));
    }
    if (resource.isNew()) {
      result.addAll(
        resource.getOutgoingEdges().stream()
          .map(ResourceEdge::getTarget)
          .flatMap(target -> collectHubEventsRecursive(target).stream())
          .collect(Collectors.toSet())
      );
    }
    return result;
  }

  private boolean isNotIndexed(Resource resource) {
    return repository.findById(resource.getId())
      .map(r -> r.getIndexDate() == null)
      .orElse(true);
  }
}
