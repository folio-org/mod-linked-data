package org.folio.linked.data.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.domain.dto.ProfileMetadata;
import org.folio.linked.data.exception.RequestProcessingExceptionBuilder;
import org.folio.linked.data.model.entity.Profile;
import org.folio.linked.data.repo.ProfileRepository;
import org.folio.linked.data.repo.ResourceTypeRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class ProfileServiceImpl implements ProfileService {

  private static final Long ID = 1L;
  private final ProfileRepository profileRepository;
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
}
