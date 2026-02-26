package org.folio.linked.data.service.profile;

import static java.util.Optional.ofNullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.domain.dto.ProfileMetadata;
import org.folio.linked.data.exception.RequestProcessingExceptionBuilder;
import org.folio.linked.data.model.entity.PreferredProfile;
import org.folio.linked.data.model.entity.ResourceTypeEntity;
import org.folio.linked.data.model.entity.pk.PreferredProfilePk;
import org.folio.linked.data.repo.PreferredProfileRepository;
import org.folio.linked.data.repo.ProfileRepository;
import org.folio.linked.data.repo.ResourceTypeRepository;
import org.folio.spring.FolioExecutionContext;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional
public class PreferredProfileServiceImpl implements PreferredProfileService {

  private final PreferredProfileRepository preferredProfileRepository;
  private final RequestProcessingExceptionBuilder exceptionBuilder;
  private final ResourceTypeRepository typeRepository;
  private final ProfileRepository profileRepository;
  private final FolioExecutionContext executionContext;

  @Override
  public void setPreferredProfile(Integer profileId, String resourceTypeUri) {
    var profile = profileRepository.findById(profileId)
      .orElseThrow(() -> exceptionBuilder.notFoundLdResourceByIdException("Profile", String.valueOf(profileId)));
    var resourceType = getResourceTypeByUri(resourceTypeUri);
    var id = new PreferredProfilePk(executionContext.getUserId(), resourceType.getHash());
    var preferredProfile = preferredProfileRepository.findById(id)
      .map(pp -> pp.setProfile(profile))
      .orElse(new PreferredProfile().setId(id).setProfile(profile));
    preferredProfileRepository.save(preferredProfile);
  }

  @Override
  public void deletePreferredProfile(String resourceTypeUri) {
    var resourceType = getResourceTypeByUri(resourceTypeUri);
    var idToDelete = new PreferredProfilePk(executionContext.getUserId(), resourceType.getHash());
    preferredProfileRepository.deleteById(idToDelete);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ProfileMetadata> getPreferredProfiles(@Nullable String resourceTypeUri) {
    var userId = executionContext.getUserId();
    var preferredProfiles = ofNullable(resourceTypeUri)
      .map(uri -> getPreferredProfile(userId, uri).stream().toList())
      .orElseGet(() -> getPreferredProfiles(userId));

    return preferredProfiles
      .stream()
      .map(PreferredProfile::getProfile)
      .map(p -> new ProfileMetadata(p.getId(), p.getName(), p.getResourceType().getUri()))
      .toList();
  }

  private List<PreferredProfile> getPreferredProfiles(UUID userId) {
    return preferredProfileRepository.findByIdUserId(userId);
  }

  private Optional<PreferredProfile> getPreferredProfile(UUID userId, String resourceTypeUri) {
    var resourceTypeId = getResourceTypeByUri(resourceTypeUri).getHash();
    return preferredProfileRepository.findById(new PreferredProfilePk(userId, resourceTypeId));
  }

  private ResourceTypeEntity getResourceTypeByUri(String resourceTypeUri) {
    return ofNullable(typeRepository.findByUri(resourceTypeUri))
      .orElseThrow(() -> exceptionBuilder.badRequestException("Invalid Resource Type in request", resourceTypeUri));
  }
}
