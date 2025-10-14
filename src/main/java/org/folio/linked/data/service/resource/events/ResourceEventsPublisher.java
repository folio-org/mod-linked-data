package org.folio.linked.data.service.resource.events;

import org.folio.linked.data.model.entity.Resource;

public interface ResourceEventsPublisher {
  void publishEventsForCreate(Resource unsavedNewResource);

  void publishEventsForUpdate(Resource unsavedNewResource);

  void publishEventsForUpdate(Resource oldResource, Resource unsavedNewResource);

  void publishEventsForDelete(Resource resourceToDelete);
}
