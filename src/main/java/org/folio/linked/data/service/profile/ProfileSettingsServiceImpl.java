package org.folio.linked.data.service.profile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.domain.dto.CustomProfileSettings;
import org.folio.linked.data.domain.dto.CustomProfileSettingsRequestDto;
import org.folio.linked.data.domain.dto.CustomProfileSettingsResponseDto;
import org.folio.linked.data.exception.RequestProcessingExceptionBuilder;
import org.folio.linked.data.model.entity.ProfileSettings;
import org.folio.linked.data.model.entity.pk.ProfileSettingsPk;
import org.folio.linked.data.repo.ProfileRepository;
import org.folio.linked.data.repo.ProfileSettingsRepository;
import org.folio.spring.FolioExecutionContext;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class ProfileSettingsServiceImpl implements ProfileSettingsService {
  private final ProfileSettingsRepository profileSettingsRepository;
  private final ProfileRepository profileRepository;
  private final RequestProcessingExceptionBuilder exceptionBuilder;
  private final FolioExecutionContext executionContext;
  private final ObjectMapper objectMapper;

  @Override
  public CustomProfileSettingsResponseDto getProfileSettings(Integer profileId) {
    var userId = executionContext.getUserId();
    profileRepository.findById(profileId)
      .orElseThrow(() -> exceptionBuilder.notFoundLdResourceByIdException("Profile", String.valueOf(profileId)));
    var settings = profileSettingsRepository.getByIdUserIdAndIdProfileId(userId, profileId);
    if (settings.isPresent()) {
      return toDto(profileId, userId, settings.get());
    }
    return defaultToProfile(profileId);
  }

  @Override
  public void setProfileSettings(Integer profileId, CustomProfileSettingsRequestDto profileSettingsRequest) {
    var userId = executionContext.getUserId();
    profileRepository.findById(profileId)
      .orElseThrow(() -> exceptionBuilder.notFoundLdResourceByIdException("Profile", String.valueOf(profileId)));
    try {
      var settings = toEntity(profileId, userId, profileSettingsRequest);
      profileSettingsRepository.save(settings);
    } catch (JsonProcessingException e) {
      throw exceptionBuilder.badRequestException("Could not process settings", String.valueOf(profileId));
    }
  }

  /**
   * In any case where the custom profile settings are not available, whether because
   * they haven't been set, they've been corrupted, or something else is wrong internally,
   * just return inactive settings. In all cases, this should lead to the profile's default
   * being used, whether for settings editing or editor rendering.
   */
  private CustomProfileSettingsResponseDto defaultToProfile(Integer profileId) {
    return new CustomProfileSettingsResponseDto(false, null, profileId);
  }

  private CustomProfileSettingsResponseDto toDto(Integer profileId, UUID userId, ProfileSettings settings) {
    try {
      var customProfileSettings = objectMapper.readValue(settings.getSettings(), CustomProfileSettings.class);
      return new CustomProfileSettingsResponseDto(
        customProfileSettings.getActive(),
        customProfileSettings.getChildren(),
        profileId);
    } catch (JsonProcessingException e) {
      log.error("Could not read stored profile settings  (user: {}, profile: {}) - default to profile",
        userId, profileId);
      return defaultToProfile(profileId);
    }
  }

  private ProfileSettings toEntity(Integer profileId, UUID userId, CustomProfileSettingsRequestDto requestDto)
      throws JsonProcessingException {
    return new ProfileSettings()
      .setId(new ProfileSettingsPk(userId, profileId))
      .setSettings(objectMapper.writeValueAsString(requestDto));
  }
}
