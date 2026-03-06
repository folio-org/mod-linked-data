package org.folio.linked.data.e2e.mappings.work.hub;

class HubReferenceSequelToIT extends AbstractHubReferenceRelationIT {

  private static final Long HUB_ID = 104L;

  @Override
  protected String relationUri() {
    return "http://bibfra.me/vocab/relation/sequelTo";
  }

  @Override
  protected Long hubId() {
    return HUB_ID;
  }
}
