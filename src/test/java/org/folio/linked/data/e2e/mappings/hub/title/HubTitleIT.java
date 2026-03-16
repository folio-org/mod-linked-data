package org.folio.linked.data.e2e.mappings.hub.title;

import org.folio.linked.data.e2e.mappings.title.AbstractTitleIT;

class HubTitleIT extends AbstractTitleIT {

  @Override
  protected String resourceUri() {
    return "http://bibfra.me/vocab/lite/Hub";
  }

  @Override
  protected String resourceId() {
    return "-7852703530478180390";
  }

  @Override
  protected String expectedResourceLabel() {
    return expectedTitleLabel();
  }
}
