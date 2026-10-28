package org.folio.linked.data.service.profile;

import java.util.List;
import org.folio.linked.data.domain.dto.CustomProfileSettingsMetadata;

public interface PreferredProfileSettingsService {
  void setPreferredProfileSettings(Integer profileId, Integer profileSettingsId);

  List<CustomProfileSettingsMetadata> getPreferredProfileSettings(Integer profileId);

  void deletePreferredProfileSettings(Integer profileId);
}
