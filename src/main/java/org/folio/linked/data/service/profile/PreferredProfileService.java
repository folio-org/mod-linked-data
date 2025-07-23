package org.folio.linked.data.service.profile;

import java.util.List;
import org.folio.linked.data.domain.dto.ProfileMetadata;
import org.springframework.lang.Nullable;

public interface PreferredProfileService {
  void setPreferredProfile(Integer profileId, String resourceTypeUri);

  List<ProfileMetadata> getPreferredProfiles(@Nullable String resourceTypeUri);
}
