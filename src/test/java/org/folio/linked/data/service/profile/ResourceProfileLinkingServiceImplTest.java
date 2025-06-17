package org.folio.linked.data.service.profile;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.folio.linked.data.domain.dto.ProfileMetadata;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceProfile;
import org.folio.linked.data.model.entity.ResourceTypeEntity;
import org.folio.linked.data.repo.ResourceProfileRepository;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class ResourceProfileLinkingServiceImplTest {

  @Mock
  private ResourceProfileRepository resourceProfileRepository;

  @Mock
  private ProfileService profileService;

  @Mock
  private FolioExecutionContext folioExecutionContext;

  @InjectMocks
  private ResourceProfileLinkingServiceImpl resourceProfileLinkingService;

  @Test
  void shouldLinkResourceToProfile() {
    // given
    var resource = new Resource().setTypes(Set.of(new ResourceTypeEntity().setUri(INSTANCE.getUri())));
    var profileId = 2;
    var captor = ArgumentCaptor.forClass(ResourceProfile.class);


    // when
    resourceProfileLinkingService.linkResourceToProfile(resource, profileId);

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
    var result = resourceProfileLinkingService.resolveProfileId(resource);

    // then
    assertThat(result).isEqualTo(profileId);
  }

  @Test
  void shouldResolveProfileId_returnUserPreferredProfile() {
    // given
    var profileId = 2;
    var userId = randomUUID();
    var resource = new Resource().setTypes(Set.of(new ResourceTypeEntity().setUri(INSTANCE.getUri()))).setId(1L);
    when(resourceProfileRepository.findProfileIdByResourceHash(resource.getId())).thenReturn(Optional.empty());
    when(folioExecutionContext.getUserId()).thenReturn(userId);
    when(profileService.getPreferredProfiles(userId, INSTANCE.getUri()))
      .thenReturn(List.of(new ProfileMetadata(profileId, "", "")));

    // when
    var result = resourceProfileLinkingService.resolveProfileId(resource);

    // then
    assertThat(result).isEqualTo(profileId);
  }

  @Test
  void shouldResolveProfileId_returnMonographProfileId() {
    // given
    var userId = randomUUID();
    var resource = new Resource().setTypes(Set.of(new ResourceTypeEntity().setUri(INSTANCE.getUri()))).setId(1L);
    when(resourceProfileRepository.findProfileIdByResourceHash(resource.getId())).thenReturn(Optional.empty());
    when(profileService.getPreferredProfiles(userId, INSTANCE.getUri())).thenReturn(List.of());
    when(folioExecutionContext.getUserId()).thenReturn(userId);

    // when
    var result = resourceProfileLinkingService.resolveProfileId(resource);

    // then
    assertThat(result).isEqualTo(1);
  }
}
