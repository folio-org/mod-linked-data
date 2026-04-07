package org.folio.linked.data.e2e.mappings.instance.title;

import org.folio.linked.data.e2e.mappings.title.AbstractTitleIT;
import org.junit.jupiter.api.BeforeEach;

class InstanceTitleIT extends AbstractTitleIT {

  @BeforeEach
  void createWork() {
    createAndSaveSampleWork();
  }

  @Override
  protected String resourceUri() {
    return "http://bibfra.me/vocab/lite/Instance";
  }

  @Override
  protected String resourceId() {
    return "-258975275057693264";
  }
}
