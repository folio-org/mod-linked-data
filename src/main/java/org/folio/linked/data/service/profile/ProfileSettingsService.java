package org.folio.linked.data.service.profile;

import org.folio.linked.data.domain.dto.ProfileSettingsRequestDto;
import org.folio.linked.data.domain.dto.ProfileSettingsResponseDto;

public interface ProfileSettingsService {
  ProfileSettingsResponseDto getProfileSettings(Integer profileId);

  void setProfileSettings(Integer profileId, ProfileSettingsRequestDto profileSettingsRequest);
}
