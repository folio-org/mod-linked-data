package org.folio.linked.data.e2e.mappings.work.hub;

class HubReferenceIsTranslationOfIT extends AbstractHubReferenceRelationIT {

  private static final Long HUB_ID = 100L;

  @Override
  protected String relationUri() {
    return "http://bibfra.me/vocab/relation/isTranslationOf";
  }

  @Override
  protected Long hubId() {
    return HUB_ID;
  }
}
