package org.folio.linked.data.e2e.mappings.work.title;

import org.folio.linked.data.e2e.mappings.title.AbstractParallelTitleIT;

class WorkParallelTitleIT extends AbstractParallelTitleIT {

  @Override
  protected String resourceUri() {
    return "http://bibfra.me/vocab/lite/Work";
  }
}
