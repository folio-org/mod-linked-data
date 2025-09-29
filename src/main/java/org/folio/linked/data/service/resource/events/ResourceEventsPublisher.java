package org.folio.linked.data.service.resource.events;

import org.folio.linked.data.model.entity.Resource;

public interface ResourceEventsPublisher {
  void publishEventsForCreate(Resource resourceToCreate);

  void publishEventsForUpdate(Resource resourceToCreate);

  void publishEventsForUpdate(Resource oldResource, Resource resourceToCreate);

  void publishEventsForDelete(Resource resourceToDelete);
}
