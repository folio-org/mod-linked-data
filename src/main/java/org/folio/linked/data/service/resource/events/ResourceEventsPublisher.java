package org.folio.linked.data.service.resource.events;

import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.service.resource.graph.SaveGraphResult;

public interface ResourceEventsPublisher {
  void emitEventsForCreate(SaveGraphResult saveGraphResult);

  void emitEventsForUpdate(SaveGraphResult saveGraphResult);

  void emitEventsForUpdate(Resource oldResource, SaveGraphResult saveGraphResult);

  void emitEventForDelete(Resource deletedResource);
}
