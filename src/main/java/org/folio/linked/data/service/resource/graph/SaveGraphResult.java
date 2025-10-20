package org.folio.linked.data.service.resource.graph;

import java.util.Set;
import org.folio.linked.data.model.entity.Resource;

public record SaveGraphResult(Resource rootResource, Set<Resource> newResources, Set<Resource> updatedResources) {
  public SaveGraphResult(Resource rootResource) {
    this(rootResource, Set.of(), Set.of());
  }
}
