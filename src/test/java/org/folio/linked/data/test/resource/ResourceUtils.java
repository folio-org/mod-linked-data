package org.folio.linked.data.test.resource;

import lombok.experimental.UtilityClass;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.service.resource.hash.HashService;

@UtilityClass
public class ResourceUtils {

  public static void setExistingResourcesIds(Resource resource, HashService hashService) {
    resource.setIdAndRefreshEdges(hashService.hash(resource));
    resource.getOutgoingEdges()
      .stream()
      .map(ResourceEdge::getTarget)
      .forEach(target -> setExistingResourcesIds(target, hashService));
  }
}
