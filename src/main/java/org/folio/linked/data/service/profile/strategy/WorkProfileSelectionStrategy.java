package org.folio.linked.data.service.profile.strategy;

import static org.folio.ld.dictionary.ResourceTypeDictionary.BOOKS;
import static org.folio.ld.dictionary.ResourceTypeDictionary.CONTINUING_RESOURCES;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.ProfileMetadata;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.service.profile.PreferredProfileService;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class WorkProfileSelectionStrategy implements ProfileSelectionStrategy {
  private static final Integer WORK_MONOGRAPH_PROFILE_ID = 2;
  private static final Integer WORK_SERIAL_PROFILE_ID = 6;

  private final PreferredProfileService preferredProfileService;

  @Override
  public boolean supports(Resource resource) {
    return resource.isOfType(WORK);
  }

  @Override
  public Integer selectProfile(Resource resource) {
    if (resource.isOfType(CONTINUING_RESOURCES)) {
      return WORK_SERIAL_PROFILE_ID;
    }
    if (resource.isOfType(BOOKS)) {
      return WORK_MONOGRAPH_PROFILE_ID;
    }
    return getUserPreferredProfileId().orElse(WORK_MONOGRAPH_PROFILE_ID);
  }

  private Optional<Integer> getUserPreferredProfileId() {
    return preferredProfileService
      .getPreferredProfiles(WORK.getUri())
      .stream()
      .findFirst()
      .map(ProfileMetadata::getId);
  }
}

