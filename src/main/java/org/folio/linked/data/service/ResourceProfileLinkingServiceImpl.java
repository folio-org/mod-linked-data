package org.folio.linked.data.service;

import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.ld.dictionary.model.ResourceType;
import org.folio.linked.data.domain.dto.ProfileMetadata;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceProfile;
import org.folio.linked.data.repo.ResourceProfileRepository;
import org.folio.spring.FolioExecutionContext;
import org.springframework.stereotype.Service;

@Service
@Log4j2
@RequiredArgsConstructor
public class ResourceProfileLinkingServiceImpl implements ResourceProfileLinkingService {
  private static final Integer MONOGRAPH_PROFILE_ID = 1;

  private final ResourceProfileRepository resourceProfileRepository;
  private final ProfileService profileService;
  private final FolioExecutionContext folioExecutionContext;

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
      .or(() -> getPreferredProfileForUser(INSTANCE))
      .or(() -> Optional.of(MONOGRAPH_PROFILE_ID));
  }

  private Optional<Integer> getPreferredProfileForUser(ResourceType resourceType) {
    return profileService.getPreferredProfiles(folioExecutionContext.getUserId(), resourceType.getUri())
      .stream()
      .findFirst()
      .map(ProfileMetadata::getId);
  }
}
