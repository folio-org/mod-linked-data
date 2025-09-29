package org.folio.linked.data.service.resource.events;

import java.util.Set;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.event.ResourceEvent;

interface ResourceEventsProvider {
  Set<ResourceEvent> getEventsForCreate(Resource unsavedNewResource);

  Set<ResourceEvent> getEventsForUpdate(Resource unsavedNewResource);

  Set<ResourceEvent> getEventsForUpdate(Resource oldResource, Resource unsavedNewResource);

  Set<ResourceEvent> getEventsForDelete(Resource resourceToDelete);
}
