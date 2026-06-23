package org.folio.linked.data.service.profile;

import static org.folio.linked.data.util.JsonUtils.JSON_MAPPER;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.domain.dto.CustomProfileSettings;
import org.folio.linked.data.domain.dto.CustomProfileSettingsMetadata;
import org.folio.linked.data.domain.dto.CustomProfileSettingsRequestDto;
import org.folio.linked.data.domain.dto.CustomProfileSettingsResponseDto;
import org.folio.linked.data.exception.RequestProcessingExceptionBuilder;
import org.folio.linked.data.model.entity.Profile;
import org.folio.linked.data.model.entity.ProfileSettings;
import org.folio.linked.data.repo.ProfileRepository;
import org.folio.linked.data.repo.ProfileSettingsRepository;
import org.folio.spring.FolioExecutionContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional
public class ProfileSettingsServiceImpl implements ProfileSettingsService {
  private final ProfileSettingsRepository profileSettingsRepository;
  private final ProfileRepository profileRepository;
  private final RequestProcessingExceptionBuilder exceptionBuilder;
  private final FolioExecutionContext executionContext;

  @Override
  @Transactional(readOnly = true)
  public List<CustomProfileSettingsMetadata> getAllProfileSettings(Integer profileId) {
    var userId = executionContext.getUserId();
    profileRepository.findById(profileId)
      .orElseThrow(() -> exceptionBuilder.notFoundLdResourceByIdException("Profile", String.valueOf(profileId)));
    var settings = profileSettingsRepository.findByUserIdAndProfileId(userId, profileId);
    return settings.stream()
      .map(profileSettings -> toMetadata(profileId, userId, profileSettings))
      .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public CustomProfileSettingsResponseDto getProfileSettings(Integer profileId, Integer profileSettingsId) {
    var userId = executionContext.getUserId();
    profileRepository.findById(profileId)
      .orElseThrow(() -> exceptionBuilder.notFoundLdResourceByIdException("Profile", String.valueOf(profileId)));
    var settings = profileSettingsRepository.findByIdAndUserId(profileSettingsId, userId);
    return settings.map(profileSettings -> toDto(profileId, userId, profileSettings))
      .orElseGet(() -> defaultToProfile(profileId, profileSettingsId));
  }

  @Override
  public CustomProfileSettingsMetadata createProfileSettings(
    Integer profileId,
    CustomProfileSettingsRequestDto profileSettingsRequest
  ) {
    var userId = executionContext.getUserId();
    var profile = profileRepository.findById(profileId)
      .orElseThrow(() -> exceptionBuilder.notFoundLdResourceByIdException("Profile", String.valueOf(profileId)));
    try {
      var settings = toEntity(profile, null, userId, profileSettingsRequest);
      var saved = profileSettingsRepository.save(settings);
      return toMetadata(profileId, userId, saved);
    } catch (JacksonException e) {
      throw exceptionBuilder.badRequestException("Could not process settings", String.valueOf(profileId));
    }
  }

  @Override
  public void setProfileSettings(
    Integer profileId,
    Integer profileSettingsId,
    CustomProfileSettingsRequestDto profileSettingsRequest
  ) {
    var userId = executionContext.getUserId();
    var profile = profileRepository.findById(profileId)
      .orElseThrow(() -> exceptionBuilder.notFoundLdResourceByIdException("Profile", String.valueOf(profileId)));
    try {
      var settings = toEntity(profile, profileSettingsId, userId, profileSettingsRequest);
      profileSettingsRepository.save(settings);
    } catch (JacksonException e) {
      throw exceptionBuilder.badRequestException("Could not process settings", String.valueOf(profileId));
    }
  }

  @Override
  public void deleteProfileSettings(Integer profileId, Integer profileSettingsId) {
    var userId = executionContext.getUserId();
    profileRepository.findById(profileId)
      .orElseThrow(() -> exceptionBuilder.notFoundLdResourceByIdException("Profile", String.valueOf(profileId)));
    profileSettingsRepository.deleteByIdAndProfileIdAndUserId(profileSettingsId, profileId, userId);
  }

  /**
   * In any case where the custom profile settings are not available, whether because
   * they haven't been set, they've been corrupted, or something else is wrong internally,
   * just return inactive settings. In all cases, this should lead to the profile's default
   * being used, whether for settings editing or editor rendering.
   */
  private CustomProfileSettingsResponseDto defaultToProfile(Integer profileId, Integer profileSettingsId) {
    return new CustomProfileSettingsResponseDto(false, null, "(defaults)", profileSettingsId, profileId);
  }

  private CustomProfileSettingsMetadata toMetadata(
    Integer profileId,
    UUID userId,
    ProfileSettings settings
  ) {
    return new CustomProfileSettingsMetadata(settings.getId(), profileId, settings.getName());
  }

  private CustomProfileSettingsResponseDto toDto(Integer profileId, UUID userId, ProfileSettings settings) {
    try {
      var customProfileSettings = JSON_MAPPER.readValue(settings.getSettings(), CustomProfileSettings.class);
      return new CustomProfileSettingsResponseDto(
        customProfileSettings.getActive(),
        customProfileSettings.getChildren(),
        settings.getName(),
        settings.getId(),
        profileId);
    } catch (JacksonException e) {
      log.error("Could not read stored profile settings  (user: {}, profile: {}, settings: {}) - default to profile",
        userId, profileId, settings.getId());
      return defaultToProfile(settings.getId(), profileId);
    }
  }

  private ProfileSettings toEntity(
    Profile profile,
    Integer profileSettingsId,
    UUID userId,
    CustomProfileSettingsRequestDto requestDto
  ) {
    return new ProfileSettings()
      .setId(profileSettingsId)
      .setName(requestDto.getName())
      .setUserId(userId)
      .setProfile(profile)
      .setSettings(JSON_MAPPER.writeValueAsString(requestDto));
  }
}
