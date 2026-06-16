package org.folio.linked.data.service.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PERSON;
import static org.folio.linked.data.test.TestUtil.emptyRequestProcessingException;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.folio.linked.data.exception.RequestProcessingException;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceProfile;
import org.folio.linked.data.model.entity.ResourceTypeEntity;
import org.folio.linked.data.repo.ResourceProfileRepository;
import org.folio.linked.data.service.profile.strategy.ProfileSelectionStrategy;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class ResourceProfileLinkingServiceImplTest {

  @Mock
  private ResourceProfileRepository resourceProfileRepository;
  @Mock
  private ProfileService profileService;

  private ResourceProfileLinkingServiceImpl profileLinkingService;

  @BeforeEach
  void setUp() {
    var strategy = new TestStrategy();
    profileLinkingService = new ResourceProfileLinkingServiceImpl(resourceProfileRepository,
      null, List.of(strategy), profileService);
  }

  @Test
  void shouldLinkResourceToProfile() {
    // given
    var resource = new Resource().setTypes(Set.of(new ResourceTypeEntity().setUri(INSTANCE.getUri())));
    var profileId = 2;
    var captor = ArgumentCaptor.forClass(ResourceProfile.class);


    // when
    profileLinkingService.linkResourceToProfile(resource, profileId);

    // then
    verify(resourceProfileRepository).save(captor.capture());
    var savedProfile = captor.getValue();
    assertThat(savedProfile.getProfileId()).isEqualTo(profileId);
    assertThat(savedProfile.getResourceHash()).isEqualTo(resource.getId());
  }

  @Test
  void shouldResolveProfileId() {
    // given
    var profileId = 2;
    var resource = new Resource().setTypes(Set.of(new ResourceTypeEntity().setUri(INSTANCE.getUri())));
    when(resourceProfileRepository.findProfileIdByResourceHash(resource.getId()))
      .thenReturn(Optional.of(() -> profileId));

    // when
    var result = profileLinkingService.resolveProfileId(resource);

    // then
    assertThat(result).isEqualTo(profileId);
  }

  @Test
  void shouldResolveProfileIdViaStrategy() {
    // given
    var resource = new Resource().setTypes(Set.of(new ResourceTypeEntity().setUri(INSTANCE.getUri())));
    when(resourceProfileRepository.findProfileIdByResourceHash(resource.getId())).thenReturn(Optional.empty());

    // when
    var result = profileLinkingService.resolveProfileId(resource);

    // then
    assertThat(result).isEqualTo(TestStrategy.TEST_PROFILE_ID);
  }

  @Test
  void shouldResolveResourceType() {
    // given
    var profileId = 13;
    when(profileService.getResourceTypeByProfileId(profileId)).thenReturn(PERSON);

    // when
    var result = profileLinkingService.resolveResourceType(profileId);

    // then
    assertThat(result).isEqualTo(PERSON);
  }

  @Test
  void shouldThrowException_whenResolveResourceTypeFails() {
    // given
    var profileId = TestStrategy.TEST_PROFILE_ID;
    when(profileService.getResourceTypeByProfileId(profileId)).thenThrow(emptyRequestProcessingException());

    // when / then
    assertThrows(RequestProcessingException.class, () -> profileLinkingService.resolveResourceType(profileId));
  }

  private static final class TestStrategy implements ProfileSelectionStrategy {
    static final Integer TEST_PROFILE_ID = 99;

    @Override
    public boolean supports(Resource resource) {
      return true;
    }

    @Override
    public Integer selectProfile(Resource resource) {
      return TEST_PROFILE_ID;
    }
  }
}
