package org.folio.linked.data.service.profile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.domain.dto.CustomProfileSettingsMetadata;
import org.folio.linked.data.exception.RequestProcessingExceptionBuilder;
import org.folio.linked.data.model.entity.PreferredProfileSettings;
import org.folio.linked.data.model.entity.pk.PreferredProfileSettingsPk;
import org.folio.linked.data.repo.PreferredProfileSettingsRepository;
import org.folio.linked.data.repo.ProfileRepository;
import org.folio.linked.data.repo.ProfileSettingsRepository;
import org.folio.spring.FolioExecutionContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional
public class PreferredProfileSettingsServiceImpl implements PreferredProfileSettingsService {

  private final ProfileRepository profileRepository;
  private final ProfileSettingsRepository profileSettingsRepository;
  private final PreferredProfileSettingsRepository preferredProfileSettingsRepository;
  private final RequestProcessingExceptionBuilder exceptionBuilder;
  private final FolioExecutionContext executionContext;

  @Override
  public void setPreferredProfileSettings(Integer profileId, Integer profileSettingsId) {
    var profile = profileRepository.findById(profileId)
      .orElseThrow(() -> exceptionBuilder.notFoundLdResourceByIdException("Profile", String.valueOf(profileId)));
    var profileSettings = profileSettingsRepository.findById(profileSettingsId)
      .orElseThrow(() -> exceptionBuilder.notFoundLdResourceByIdException(
        "ProfileSettings",
        String.valueOf(profileSettingsId)
      ));
    var id = new PreferredProfileSettingsPk(executionContext.getUserId(), profileId);
    var preferredProfileSettings = preferredProfileSettingsRepository.findById(id)
      .map(pps -> pps.setProfileSettings(profileSettings))
      .orElse(new PreferredProfileSettings().setId(id).setProfile(profile).setProfileSettings(profileSettings));
    preferredProfileSettingsRepository.save(preferredProfileSettings);
  }

  @Override
  public void deletePreferredProfileSettings(Integer profileId) {
    var idToDelete = new PreferredProfileSettingsPk(executionContext.getUserId(), profileId);
    preferredProfileSettingsRepository.deleteById(idToDelete);
  }

  @Override
  public List<CustomProfileSettingsMetadata> getPreferredProfileSettings(Integer profileId) {
    var userId = executionContext.getUserId();
    var preferredProfileSettings = getPreferredProfileSettings(userId, profileId).stream().toList();
    
    return preferredProfileSettings
      .stream()
      .map(PreferredProfileSettings::getProfileSettings)
      .map(p -> new CustomProfileSettingsMetadata(p.getId(), p.getProfile().getId(), p.getName()))
      .toList();
  }

  private Optional<PreferredProfileSettings> getPreferredProfileSettings(UUID userId, Integer profileId) {
    return preferredProfileSettingsRepository.findById(new PreferredProfileSettingsPk(userId, profileId));
  }
}
