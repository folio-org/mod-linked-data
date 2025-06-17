package org.folio.linked.data.service;

import static java.util.Optional.ofNullable;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.ld.dictionary.model.ResourceType;
import org.folio.linked.data.domain.dto.ProfileMetadata;
import org.folio.linked.data.exception.RequestProcessingExceptionBuilder;
import org.folio.linked.data.model.entity.PreferredProfile;
import org.folio.linked.data.model.entity.Profile;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceProfile;
import org.folio.linked.data.model.entity.pk.PreferredProfilePk;
import org.folio.linked.data.repo.PreferredProfileRepository;
import org.folio.linked.data.repo.ProfileRepository;
import org.folio.linked.data.repo.ResourceProfileRepository;
import org.folio.linked.data.repo.ResourceTypeRepository;
import org.folio.spring.FolioExecutionContext;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class ProfileServiceImpl implements ProfileService {
  private static final Integer MONOGRAPH_PROFILE_ID = 1;
  private final ProfileRepository profileRepository;
  private final PreferredProfileRepository preferredProfileRepository;
  private final ResourceTypeRepository typeRepository;
  private final RequestProcessingExceptionBuilder exceptionBuilder;
  private final ResourceProfileRepository resourceProfileRepository;
  private final FolioExecutionContext folioExecutionContext;

  @Override
  public Profile saveProfile(Integer id, String name, String resourceTypeUri, String value) {
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
    return getProfileById(MONOGRAPH_PROFILE_ID);
  }

  @Override
  public String getProfileById(Integer id) {
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
  public void setPreferredProfile(UUID userId, Integer profileId, String resourceTypeUri) {
    var profile =  profileRepository.findById(profileId)
      .orElseThrow(() -> exceptionBuilder.notFoundLdResourceByIdException("Profile", String.valueOf(profileId)));
    var resourceType = typeRepository.findByUri(resourceTypeUri);
    var preferredProfile = new PreferredProfile()
      .setId(new PreferredProfilePk(userId, resourceType.getHash()))
      .setProfile(profile);
    log.info("Setting preferred profile for user {}: profileId={}, resourceTypeUri={}", userId, profileId,
      resourceTypeUri);
    preferredProfileRepository.save(preferredProfile);
  }

  @Override
  public void linkResourceToProfile(Resource resource, Integer profileId) {
    if (resource.isNotOfType(INSTANCE)) {
      return;
    }
    if (profileId != null) {
      log.info("Linking resource {} to profile {}", resource.getId(), profileId);
      resourceProfileRepository.save(new ResourceProfile(resource.getId(), profileId));
    }
  }

  @Override
  public Optional<Integer> getLinkedProfile(Resource resource) {
    if (resource.isNotOfType(INSTANCE)) {
      return Optional.empty();
    }
    return resourceProfileRepository.findById(resource.getId())
      .map(ResourceProfile::getProfileId)
      .or(() -> getPreferredProfileIdForUser(INSTANCE))
      .or(() -> Optional.of(MONOGRAPH_PROFILE_ID));
  }

  private Optional<Integer> getPreferredProfileIdForUser(ResourceType resourceType) {
    return getPreferredProfile(folioExecutionContext.getUserId(), resourceType.getUri())
      .map(preferredProfile ->  preferredProfile.getProfile().getId());
  }

  @Override
  public List<ProfileMetadata> getPreferredProfiles(UUID userId, @Nullable String resourceTypeUri) {
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
    var resourceTypeId = typeRepository.findByUri(resourceTypeUri).getHash();
    return preferredProfileRepository.findById(new PreferredProfilePk(userId, resourceTypeId));
  }
}
