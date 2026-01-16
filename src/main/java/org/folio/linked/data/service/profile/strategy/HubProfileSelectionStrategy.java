package org.folio.linked.data.service.profile.strategy;

import static org.folio.ld.dictionary.ResourceTypeDictionary.HUB;

import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
public class HubProfileSelectionStrategy implements ProfileSelectionStrategy {
  private static final Integer HUB_PROFILE_ID = 7;

  @Override
  public boolean supports(Resource resource) {
    return resource.isOfType(HUB);
  }

  @Override
  public Integer selectProfile(Resource resource) {
    return HUB_PROFILE_ID;
  }
}

