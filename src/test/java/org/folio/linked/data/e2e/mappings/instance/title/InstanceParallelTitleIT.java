package org.folio.linked.data.e2e.mappings.instance.title;

import org.folio.linked.data.e2e.mappings.title.AbstractParallelTitleIT;
import org.junit.jupiter.api.BeforeEach;

class InstanceParallelTitleIT extends AbstractParallelTitleIT {

  @BeforeEach
  void createWork() {
    createAndSaveSampleWork();
  }

  @Override
  protected String resourceUri() {
    return "http://bibfra.me/vocab/lite/Instance";
  }
}
