package org.folio.linked.data.e2e.mappings.work.hub;

class HubReferenceRelatedWorkIT extends AbstractHubReferenceRelationIT {

  private static final Long HUB_ID = 101L;

  @Override
  protected String relationUri() {
    return "http://bibfra.me/vocab/relation/relatedWork";
  }

  @Override
  protected Long hubId() {
    return HUB_ID;
  }
}
