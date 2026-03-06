package org.folio.linked.data.e2e.mappings.work.hub;

class HubReferenceAdaptedAsMotionPictureIT extends AbstractHubReferenceRelationIT {

  private static final Long HUB_ID = 102L;

  @Override
  protected String relationUri() {
    return "http://bibfra.me/vocab/relation/adaptedAsMotionPicture";
  }

  @Override
  protected Long hubId() {
    return HUB_ID;
  }
}
