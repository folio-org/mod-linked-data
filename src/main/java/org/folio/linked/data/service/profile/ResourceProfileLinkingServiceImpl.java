package org.folio.linked.data.service.profile;

import static org.folio.linked.data.util.ResourceUtils.getTypeUris;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.exception.RequestProcessingExceptionBuilder;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceProfile;
import org.folio.linked.data.repo.ResourceProfileRepository;
import org.folio.linked.data.service.profile.strategy.ProfileSelectionStrategy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Log4j2
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ResourceProfileLinkingServiceImpl implements ResourceProfileLinkingService {
  private final ResourceProfileRepository resourceProfileRepository;
  private final RequestProcessingExceptionBuilder exceptionBuilder;
  private final List<ProfileSelectionStrategy> profileSelectionStrategies;

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
    return profileSelectionStrategies.stream()
      .filter(strategy -> strategy.supports(resource))
      .findFirst()
      .map(strategy -> strategy.selectProfile(resource))
      .orElseThrow(
        () -> exceptionBuilder.notSupportedException(String.join(", ", getTypeUris(resource)), "Profile Linking")
      );
  }
}
