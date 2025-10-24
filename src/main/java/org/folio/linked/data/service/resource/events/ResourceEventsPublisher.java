package org.folio.linked.data.service.resource.events;

import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.service.resource.graph.SaveGraphResult;

public interface ResourceEventsPublisher {

  void emitEventsForCreateAndUpdate(SaveGraphResult saveGraphResult, Resource oldResource);

  void emitEventForDelete(Resource deletedResource);
}
