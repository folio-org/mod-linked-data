package org.folio.linked.data.service.profile;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.folio.linked.data.util.Constants.Cache.PROFILES;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.domain.dto.ProfileMetadata;
import org.folio.linked.data.exception.RequestProcessingExceptionBuilder;
import org.folio.linked.data.model.entity.Profile;
import org.folio.linked.data.model.entity.ResourceTypeEntity;
import org.folio.linked.data.repo.ProfileRepository;
import org.folio.linked.data.repo.ResourceTypeRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional(readOnly = true)
public class ProfileServiceImpl implements ProfileService {
  private static final String PROFILE_DIRECTORY = "profiles";

  private final ProfileRepository profileRepository;
  private final ResourceTypeRepository typeRepository;
  private final RequestProcessingExceptionBuilder exceptionBuilder;
  private final ObjectMapper objectMapper;

  @Override
  @Transactional
  public void saveAllProfiles() {
    var profilesDirectory = getClass().getClassLoader().getResource(PROFILE_DIRECTORY);
    try (var files = Files.list(Paths.get(profilesDirectory.toURI()))) {
      files
        .filter(Files::isRegularFile)
        .forEach(this::saveProfile);
    } catch (IOException | URISyntaxException | UnsupportedOperationException e) {
      log.error("Failed to read profiles from directory: {}", PROFILE_DIRECTORY, e);
    }
  }

  @Override
  @Cacheable(value = PROFILES, key = "#id")
  public Profile getProfileById(Integer id) {
    return profileRepository.findById(id)
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

  private void saveProfile(Path file) {
    try (var inputStream = Files.newInputStream(file)) {
      var profile = objectMapper.readValue(inputStream, ProfileDto.class);
      var profileEntity = toProfileEntity(profile);
      profileRepository.save(profileEntity);
    } catch (IOException e) {
      log.error("Failed to process file: {}", file, e);
    }
  }

  private Profile toProfileEntity(ProfileDto profileDto) throws JsonProcessingException {
    var resourceType = typeRepository.findByUri(profileDto.resourceType());
    var profileContent = objectMapper.writeValueAsString(profileDto.value());
    var additionalTypes = isEmpty(profileDto.additionalResourceTypes())
      ? List.<ResourceTypeEntity>of()
      : profileDto.additionalResourceTypes().stream().map(typeRepository::findByUri).toList();

    return new Profile()
      .setId(profileDto.id())
      .setName(profileDto.name())
      .setResourceType(resourceType)
      .setValue(profileContent)
      .setAdditionalResourceTypes(additionalTypes);
  }

  record ProfileDto(
    Integer id,
    String name,
    String resourceType,
    List<String> additionalResourceTypes,
    JsonNode value) {
  }
}
