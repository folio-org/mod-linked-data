package org.folio.linked.data.service.profile;

import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.folio.linked.data.util.ResourceUtils.getTypeUris;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.ld.dictionary.model.ResourceType;
import org.folio.linked.data.domain.dto.ProfileMetadata;
import org.folio.linked.data.exception.RequestProcessingExceptionBuilder;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceProfile;
import org.folio.linked.data.repo.ResourceProfileRepository;
import org.folio.spring.FolioExecutionContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Log4j2
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ResourceProfileLinkingServiceImpl implements ResourceProfileLinkingService {
  private static final Integer WORK_PROFILE_ID = 2;
  private static final Integer MONOGRAPH_PROFILE_ID = 3;

  private final ResourceProfileRepository resourceProfileRepository;
  private final PreferredProfileService preferredProfileService;
  private final FolioExecutionContext executionContext;
  private final RequestProcessingExceptionBuilder exceptionBuilder;

  @Override
  @Transactional
  public void linkResourceToProfile(Resource resource, Integer profileId) {
    if (profileId == null) {
      return;
    }
    log.info("Linking resource {} to profile {}", resource.getId(), profileId);
    resourceProfileRepository.save(new ResourceProfile(resource.getId(), profileId));
  }

  @Override
  public Integer resolveProfileId(Resource resource) {
    var resourceType = getResourceType(resource);
    return resourceProfileRepository.findProfileIdByResourceHash(resource.getId())
      .map(ResourceProfileRepository.ProfileIdProjection::getProfileId)
      .or(() -> getUserPreferredProfileId(resourceType))
      .orElseGet(() -> getDefaultProfileId(resourceType));
  }

  private Integer getDefaultProfileId(ResourceType resourceType) {
    if (resourceType == WORK) {
      return WORK_PROFILE_ID;
    }
    return MONOGRAPH_PROFILE_ID;
  }

  private Optional<Integer> getUserPreferredProfileId(ResourceType resourceType) {
    return preferredProfileService
      .getPreferredProfiles(executionContext.getUserId(), resourceType.getUri())
      .stream()
      .findFirst()
      .map(ProfileMetadata::getId);
  }

  private ResourceType getResourceType(Resource resource) {
    if (resource.isOfType(INSTANCE)) {
      return INSTANCE;
    }
    if (resource.isOfType(WORK)) {
      return WORK;
    }
    throw exceptionBuilder.notSupportedException(String.join(", ", getTypeUris(resource)), "Profile Linking");
  }
}
