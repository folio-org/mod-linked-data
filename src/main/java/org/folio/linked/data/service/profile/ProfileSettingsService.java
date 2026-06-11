package org.folio.linked.data.service.profile;

import java.util.List;
import org.folio.linked.data.domain.dto.CustomProfileSettingsMetadata;
import org.folio.linked.data.domain.dto.CustomProfileSettingsRequestDto;
import org.folio.linked.data.domain.dto.CustomProfileSettingsResponseDto;

public interface ProfileSettingsService {
  List<CustomProfileSettingsMetadata> getAllProfileSettings(Integer profileId);

  CustomProfileSettingsResponseDto getProfileSettings(Integer profileId, Integer profileSettingsId);

  void setProfileSettings(
    Integer profileId,
    Integer profileSettingsId,
    CustomProfileSettingsRequestDto profileSettingsRequest
  );
}
