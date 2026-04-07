package org.folio.linked.data.e2e.mappings.instance.title;

import org.folio.linked.data.e2e.mappings.title.AbstractVariantTitleIT;
import org.junit.jupiter.api.BeforeEach;

class InstanceVariantTitleIT extends AbstractVariantTitleIT {

  @BeforeEach
  void createWork() {
    createAndSaveSampleWork();
  }

  @Override
  protected String resourceUri() {
    return "http://bibfra.me/vocab/lite/Instance";
  }
}
