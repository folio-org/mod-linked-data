package org.folio.linked.data.service.resource.copy;

import org.folio.linked.data.model.entity.Resource;

public interface ResourceCopyService {

  void copyEdgesAndProperties(Resource old, Resource updated);
}
