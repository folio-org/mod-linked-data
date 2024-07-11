package org.folio.linked.data.service.resource.meta;

import org.folio.linked.data.model.entity.InstanceMetadata;
import org.folio.linked.data.model.entity.Resource;

public interface MetadataService {

  void ensureMetadata(Resource resource);

  void ensureMetadata(Resource newResource, InstanceMetadata oldResourceMeta);
}
