package org.folio.linked.data.service.profile;

import static java.util.stream.Collectors.toSet;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.folio.linked.data.util.Constants.Cache.PROFILES;
import static org.folio.linked.data.util.JsonUtils.JSON_MAPPER;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.domain.dto.ProfileMetadata;
import org.folio.linked.data.exception.RequestProcessingExceptionBuilder;
import org.folio.linked.data.model.entity.Profile;
import org.folio.linked.data.model.entity.ResourceTypeEntity;
import org.folio.linked.data.repo.ProfileRepository;
import org.folio.linked.data.repo.ResourceTypeRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional(readOnly = true)
public class ProfileServiceImpl implements ProfileService {
  private static final String PROFILES_PATTERN = "classpath*:profiles/*.json";
  private final ProfileRepository profileRepository;
  private final ResourceTypeRepository typeRepository;
  private final RequestProcessingExceptionBuilder exceptionBuilder;
  private final ResourcePatternResolver resourcePatternResolver;

  @Override
  @Transactional
  public void saveAllProfiles() {
    try {
      var resources = resourcePatternResolver.getResources(PROFILES_PATTERN);
      for (var resource : resources) {
        saveProfile(resource);
      }
    } catch (IOException e) {
      log.error("Failed to read profiles with pattern: {}", PROFILES_PATTERN, e);
    }
  }

  @Override
  @Cacheable(value = PROFILES, key = "@folioExecutionContext.tenantId + '_' + #id")
  public Profile getProfileById(Integer id) {
    return profileRepository.findByIdWithResourceTypes(id)
      .orElseThrow(() -> exceptionBuilder.notFoundLdResourceByIdException("Profile", String.valueOf(id)));
  }

  @Override
  public List<ProfileMetadata> getMetadataByResourceType(String resourceTypeUri) {
    return profileRepository
      .findByResourceTypeUriOrderByIdAsc(resourceTypeUri)
      .stream()
      .map(profile -> new ProfileMetadata(profile.getId(), profile.getName(), profile.getResourceType().getUri()))
      .toList();
  }

  private void saveProfile(Resource resource) {
    try (var inputStream = resource.getInputStream()) {
      var profile = JSON_MAPPER.readValue(inputStream, ProfileDto.class);
      var profileEntity = toProfileEntity(profile);
      profileRepository.save(profileEntity);
    } catch (IOException e) {
      log.error("Failed to process profile resource: {}", resource, e);
    }
  }

  private Profile toProfileEntity(ProfileDto profileDto) {
    var resourceType = typeRepository.findByUri(profileDto.resourceType());
    var profileContent = JSON_MAPPER.writeValueAsString(profileDto.value());
    var additionalTypes = isEmpty(profileDto.additionalResourceTypes())
      ? Set.<ResourceTypeEntity>of()
      : profileDto.additionalResourceTypes().stream().map(typeRepository::findByUri).collect(toSet());

    return new Profile()
      .setId(profileDto.id())
      .setName(profileDto.name())
      .setResourceType(resourceType)
      .setValue(profileContent)
      .setAdditionalResourceTypes(additionalTypes);
  }

  private record ProfileDto(
    Integer id,
    String name,
    String resourceType,
    Set<String> additionalResourceTypes,
    JsonNode value) { }
}
