package org.folio.linked.data.e2e.mappings.work.title;

import org.folio.linked.data.e2e.mappings.title.AbstractTitleIT;
import org.folio.linked.data.model.entity.Resource;

class WorkTitleIT extends AbstractTitleIT {

  @Override
  protected String resourceUri() {
    return "http://bibfra.me/vocab/lite/Work";
  }

  @Override
  protected String resourceId() {
    return "-5922785341204753106";
  }

  @Override
  protected void validateRootResourceType(Resource resource) {
    validateResourceType(resource, resourceUri(), "http://bibfra.me/vocab/library/Books");
  }
}
