package org.folio.linked.data.service.profile.strategy;

import org.folio.linked.data.model.entity.Resource;

public interface ProfileSelectionStrategy {
  boolean supports(Resource resource);

  Integer selectProfile(Resource resource);
}

