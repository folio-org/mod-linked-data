package org.folio.linked.data.service.profile;

import static org.folio.ld.dictionary.PredicateDictionary.BOOK_FORMAT;
import static org.folio.ld.dictionary.ResourceTypeDictionary.BOOKS;
import static org.folio.ld.dictionary.ResourceTypeDictionary.CONTINUING_RESOURCES;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.folio.linked.data.util.ResourceUtils.extractWorkFromInstance;
import static org.folio.linked.data.util.ResourceUtils.getTypeUris;
import static org.folio.linked.data.util.ResourceUtils.hasEdge;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.ld.dictionary.model.ResourceType;
import org.folio.linked.data.domain.dto.ProfileMetadata;
import org.folio.linked.data.exception.RequestProcessingExceptionBuilder;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceProfile;
import org.folio.linked.data.repo.ResourceProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Log4j2
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ResourceProfileLinkingServiceImpl implements ResourceProfileLinkingService {
  private static final Integer WORK_MONOGRAPH_PROFILE_ID = 2;
  private static final Integer WORK_SERIAL_PROFILE_ID = 6;
  private static final Integer INSTANCE_MONOGRAPH_PROFILE_ID = 3;
  private static final Integer INSTANCE_RARE_BOOKS_PROFILE_ID = 4;
  private static final Integer INSTANCE_SERIAL_PROFILE_ID = 5;

  private final ResourceProfileRepository resourceProfileRepository;
  private final PreferredProfileService preferredProfileService;
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
    return resourceProfileRepository.findProfileIdByResourceHash(resource.getId())
      .map(ResourceProfileRepository.ProfileIdProjection::getProfileId)
      .orElseGet(() -> selectAppropriateProfile(resource));
  }

  private Integer selectAppropriateProfile(Resource resource) {
    if (resource.isOfType(WORK)) {
      if (resource.isOfType(CONTINUING_RESOURCES)) {
        return WORK_SERIAL_PROFILE_ID;
      }
      if (resource.isOfType(BOOKS)) {
        return WORK_MONOGRAPH_PROFILE_ID;
      }
      return getUserPreferredProfileId(resource).orElse(WORK_MONOGRAPH_PROFILE_ID);
    }
    if (resource.isOfType(INSTANCE)) {
      if (hasEdge(resource, BOOK_FORMAT)) {
        return INSTANCE_RARE_BOOKS_PROFILE_ID;
      }
      return extractWorkFromInstance(resource)
        .stream()
        .anyMatch(pw -> pw.isOfType(CONTINUING_RESOURCES))
        ? INSTANCE_SERIAL_PROFILE_ID
        : getUserPreferredProfileId(resource).orElse(INSTANCE_MONOGRAPH_PROFILE_ID);
    }
    throw exceptionBuilder.notSupportedException(String.join(", ", getTypeUris(resource)), "Profile Linking");
  }

  private Optional<Integer> getUserPreferredProfileId(Resource resource) {
    return preferredProfileService
      .getPreferredProfiles(getResourceType(resource).getUri())
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
