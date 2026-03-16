package org.folio.linked.data.e2e.mappings.hub.title;

import org.folio.linked.data.e2e.mappings.title.AbstractVariantTitleIT;

class HubVariantTitleIT extends AbstractVariantTitleIT {

  @Override
  protected String resourceUri() {
    return "http://bibfra.me/vocab/lite/Hub";
  }
}
