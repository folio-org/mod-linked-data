package org.folio.linked.data.service.profile;

import java.util.List;
import java.util.UUID;
import org.folio.linked.data.domain.dto.ProfileMetadata;
import org.springframework.lang.Nullable;

public interface PreferredProfileService {
  void setPreferredProfile(UUID userId, Integer profileId, String resourceTypeUri);

  List<ProfileMetadata> getPreferredProfiles(UUID userId, @Nullable String resourceTypeUri);
}
