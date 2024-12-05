package org.folio.linked.data.service.resource;

import org.folio.linked.data.model.entity.Resource;

public interface ResourceEdgeService {
  void copyOutgoingEdges(Resource from, Resource to);
}
