package org.folio.linked.data.service.profile;

import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.ld.dictionary.model.ResourceType;
import org.folio.linked.data.domain.dto.ProfileMetadata;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceProfile;
import org.folio.linked.data.repo.ResourceProfileRepository;
import org.folio.linked.data.repo.ResourceRepository;
import org.folio.spring.FolioExecutionContext;
import org.springframework.stereotype.Service;

@Service
@Log4j2
@RequiredArgsConstructor
public class ResourceProfileLinkingServiceImpl implements ResourceProfileLinkingService {
  private static final Integer MONOGRAPH_PROFILE_ID = 1;

  private final ResourceProfileRepository resourceProfileRepository;
  private final ProfileService profileService;
  private final FolioExecutionContext executionContext;
  private final ResourceRepository resourceRepository;

  @Override
  public void linkResourceToProfile(Resource resource, Integer profileId) {
    if (profileId == null) {
      return;
    }
    log.info("Linking resource {} to profile {}", resource.getId(), profileId);
    resourceProfileRepository.save(new ResourceProfile(resource.getId(), profileId));
  }

  @Override
  public Integer resolveProfileId(Resource resource) {
    return resourceProfileRepository.findById(resource.getId())
      .map(ResourceProfile::getProfileId)
      .or(() -> getPreferredProfileId(resource))
      .orElse(MONOGRAPH_PROFILE_ID);
  }

  @Override
  public Integer resolveProfileId(Long resourceId) {
    return resourceProfileRepository.findById(resourceId)
      .map(ResourceProfile::getProfileId)
      .or(() -> getPreferredProfileId(resourceId))
      .orElse(MONOGRAPH_PROFILE_ID);
  }

  private Optional<Integer> getPreferredProfileId(Long resourceId) {
    return resourceRepository.findById(resourceId)
      .flatMap(this::getPreferredProfileId);
  }

  private Optional<Integer> getPreferredProfileId(Resource resource) {
    return getPreferredProfileId(getResourceType(resource));
  }

  private Optional<Integer> getPreferredProfileId(ResourceType type) {
    return profileService
      .getPreferredProfiles(executionContext.getUserId(), type.getUri())
      .stream()
      .findFirst()
      .map(ProfileMetadata::getId);
  }

  private static ResourceType getResourceType(Resource resource) {
    if (resource.isOfType(INSTANCE)) {
      return INSTANCE;
    } else if (resource.isOfType(WORK)) {
      return WORK;
    }
    throw new IllegalArgumentException("Resource type do not support profiles: " + resource.getTypes());
  }
}
