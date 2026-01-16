package org.folio.linked.data.service.profile.strategy;

import static org.folio.ld.dictionary.PredicateDictionary.BOOK_FORMAT;
import static org.folio.ld.dictionary.ResourceTypeDictionary.CONTINUING_RESOURCES;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.linked.data.util.ResourceUtils.extractWorkFromInstance;
import static org.folio.linked.data.util.ResourceUtils.hasEdge;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.ProfileMetadata;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.service.profile.PreferredProfileService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InstanceProfileSelectionStrategy implements ProfileSelectionStrategy {
  private static final Integer INSTANCE_MONOGRAPH_PROFILE_ID = 3;
  private static final Integer INSTANCE_RARE_BOOKS_PROFILE_ID = 4;
  private static final Integer INSTANCE_SERIAL_PROFILE_ID = 5;

  private final PreferredProfileService preferredProfileService;

  @Override
  public boolean supports(Resource resource) {
    return resource.isOfType(INSTANCE);
  }

  @Override
  public Integer selectProfile(Resource resource) {
    if (hasEdge(resource, BOOK_FORMAT)) {
      return INSTANCE_RARE_BOOKS_PROFILE_ID;
    }
    boolean isSerial = extractWorkFromInstance(resource)
      .stream()
      .anyMatch(pw -> pw.isOfType(CONTINUING_RESOURCES));
    if (isSerial) {
      return INSTANCE_SERIAL_PROFILE_ID;
    }
    return getUserPreferredProfileId().orElse(INSTANCE_MONOGRAPH_PROFILE_ID);
  }

  private Optional<Integer> getUserPreferredProfileId() {
    return preferredProfileService
      .getPreferredProfiles(INSTANCE.getUri())
      .stream()
      .findFirst()
      .map(ProfileMetadata::getId);
  }
}

