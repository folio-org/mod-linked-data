package org.folio.linked.data.e2e.mappings.work.hub;

class HubReferenceSequelIT extends AbstractHubReferenceRelationIT {

  private static final Long HUB_ID = 103L;

  @Override
  protected String relationUri() {
    return "http://bibfra.me/vocab/relation/sequel";
  }

  @Override
  protected Long hubId() {
    return HUB_ID;
  }
}
