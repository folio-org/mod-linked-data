package org.folio.linked.data.e2e.mappings.instance.title;

import org.folio.linked.data.e2e.mappings.title.AbstractParallelTitleIT;

class InstanceParallelTitleIT extends AbstractParallelTitleIT {

  @Override
  protected String resourceUri() {
    return "http://bibfra.me/vocab/lite/Instance";
  }
}
