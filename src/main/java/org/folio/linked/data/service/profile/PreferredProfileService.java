package org.folio.linked.data.service.profile;

import java.util.List;
import org.folio.linked.data.domain.dto.ProfileMetadata;
import org.jspecify.annotations.Nullable;

public interface PreferredProfileService {
  void setPreferredProfile(Integer profileId, String resourceTypeUri);

  List<ProfileMetadata> getPreferredProfiles(@Nullable String resourceTypeUri);

  void deletePreferredProfile(String resourceTypeUri);
}
