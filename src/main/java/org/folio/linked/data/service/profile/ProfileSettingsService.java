package org.folio.linked.data.service.profile;

import org.folio.linked.data.domain.dto.CustomProfileSettingsRequestDto;
import org.folio.linked.data.domain.dto.CustomProfileSettingsResponseDto;

public interface ProfileSettingsService {
  CustomProfileSettingsResponseDto getProfileSettings(Integer profileId);

  void setProfileSettings(Integer profileId, CustomProfileSettingsRequestDto profileSettingsRequest);
}
