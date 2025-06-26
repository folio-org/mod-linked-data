package org.folio.linked.data.service.profile;

import org.folio.linked.data.model.entity.Resource;

public interface ResourceProfileLinkingService {
  void linkResourceToProfile(Resource resource, Integer profileId);

  Integer resolveProfileId(Resource resource);
}
