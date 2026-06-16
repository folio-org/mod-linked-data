package org.folio.linked.data.service.profile.strategy;

import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.model.entity.Resource;

public interface ProfileSelectionStrategy {
  boolean supports(Resource resource);

  Integer selectProfile(Resource resource);

  boolean supportsProfileId(Integer profileId);

  ResourceTypeDictionary selectResourceType(Integer profileId);
}
