package org.folio.linked.data.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.domain.dto.ProfileMetadata;
import org.folio.linked.data.exception.RequestProcessingExceptionBuilder;
import org.folio.linked.data.model.entity.PreferredProfile;
import org.folio.linked.data.model.entity.Profile;
import org.folio.linked.data.model.entity.pk.PreferredProfilePk;
import org.folio.linked.data.repo.PreferredProfileRepository;
import org.folio.linked.data.repo.ProfileRepository;
import org.folio.linked.data.repo.ResourceTypeRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class ProfileServiceImpl implements ProfileService {

  private static final Long ID = 1L;
  private final ProfileRepository profileRepository;
  private final PreferredProfileRepository preferredProfileRepository;
  private final ResourceTypeRepository typeRepository;
  private final RequestProcessingExceptionBuilder exceptionBuilder;

  @Override
  public Profile saveProfile(Long id, String name, String resourceTypeUri, String value) {
    log.info("Creating / updating profile. ID: {}, name: {}, resourceType: {}.", id, name, resourceTypeUri);
    var resourceType = typeRepository.findByUri(resourceTypeUri);
    var profile = new Profile()
      .setId(id)
      .setName(name)
      .setResourceType(resourceType)
      .setValue(value);
    return profileRepository.save(profile);
  }

  @Override
  public String getProfile() {
    return profileRepository.findById(ID)
      .map(Profile::getValue)
      .orElseThrow(() -> exceptionBuilder.notFoundLdResourceByIdException("Profile", String.valueOf(ID)));
  }

  @Override
  public String getProfileById(Long id) {
    return profileRepository.findById(id)
      .map(Profile::getValue)
      .orElseThrow(() -> exceptionBuilder.notFoundLdResourceByIdException("Profile", String.valueOf(id)));
  }

  @Override
  public List<ProfileMetadata> getMetadataByResourceType(String resourceTypeUri) {
    return profileRepository
      .findByResourceTypeUri(resourceTypeUri)
      .stream()
      .map(profile -> new ProfileMetadata(profile.getId(), profile.getName(), profile.getResourceType().getUri()))
      .toList();
  }

  @Override
  public List<ProfileMetadata> getPreferredProfile(UUID userId, String resourceTypeUri) {
    return getPreferredProfiles(userId, resourceTypeUri)
      .map(PreferredProfile::getProfile)
      .map(p -> new ProfileMetadata(p.getId(), p.getName(), p.getResourceType().getUri()))
      .toList();
  }

  @Override
  public void setPreferredProfile(UUID userId, Long profileId, String resourceTypeUri) {
    var profile =  profileRepository.findById(profileId)
      .orElseThrow(() -> exceptionBuilder.notFoundLdResourceByIdException("Profile", String.valueOf(profileId)));
    var resourceType = typeRepository.findByUri(resourceTypeUri);
    var preferredProfile = new PreferredProfile()
      .setId(new PreferredProfilePk(userId, resourceType.getHash()))
      .setProfile(profile)
      .setResourceType(resourceType);
    preferredProfileRepository.save(preferredProfile);
  }

  private Stream<PreferredProfile> getPreferredProfiles(UUID userId, String resourceTypeUri) {
    if (resourceTypeUri == null) {
      return preferredProfileRepository.findByIdUserId(userId).stream();
    }
    Long resourceTypeId = typeRepository.findByUri(resourceTypeUri).getHash();
    return preferredProfileRepository.findById(new PreferredProfilePk(userId, resourceTypeId)).stream();
  }
}
