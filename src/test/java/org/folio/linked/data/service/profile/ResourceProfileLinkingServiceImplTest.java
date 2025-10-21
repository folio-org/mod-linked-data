package org.folio.linked.data.service.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.PredicateDictionary.BOOK_FORMAT;
import static org.folio.ld.dictionary.PredicateDictionary.INSTANTIATES;
import static org.folio.ld.dictionary.ResourceTypeDictionary.BOOKS;
import static org.folio.ld.dictionary.ResourceTypeDictionary.CONTINUING_RESOURCES;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.domain.dto.ProfileMetadata;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.model.entity.ResourceProfile;
import org.folio.linked.data.model.entity.ResourceTypeEntity;
import org.folio.linked.data.repo.ResourceProfileRepository;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
  private PreferredProfileService preferredProfileService;

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
    var resource = new Resource()
      .setTypes(Set.of(new ResourceTypeEntity().setUri(INSTANCE.getUri())))
      .setIdAndRefreshEdges(1L);
    when(resourceProfileRepository.findProfileIdByResourceHash(resource.getId())).thenReturn(Optional.empty());
    when(preferredProfileService.getPreferredProfiles(INSTANCE.getUri()))
      .thenReturn(List.of(new ProfileMetadata(profileId, "", "")));

    // when
    var result = resourceProfileLinkingService.resolveProfileId(resource);

    // then
    assertThat(result).isEqualTo(profileId);
  }

  @ParameterizedTest
  @MethodSource("dataProvider")
  void shouldResolveProfileId_returnDefaultProfileId(int expectedProfileId, Resource resource) {
    // given
    when(resourceProfileRepository.findProfileIdByResourceHash(resource.getId())).thenReturn(Optional.empty());
    lenient().when(preferredProfileService.getPreferredProfiles(any())).thenReturn(List.of());

    // when
    var result = resourceProfileLinkingService.resolveProfileId(resource);

    // then
    assertThat(result).isEqualTo(expectedProfileId);
  }

  private static Stream<Arguments> dataProvider() {
    return Stream.of(
      Arguments.of(2, getWork(BOOKS)),
      Arguments.of(3, getInstance()),
      Arguments.of(4, getInstance().addOutgoingEdge(getBookFormatEdge())),
      Arguments.of(5, getInstance().addOutgoingEdge(getSerialWorkEdge())),
      Arguments.of(6, getWork(CONTINUING_RESOURCES))
    );
  }

  private static Resource getWork(ResourceTypeDictionary extraType) {
    return new Resource().setIdAndRefreshEdges(1L).addTypes(WORK, extraType);
  }

  private static Resource getInstance() {
    return new Resource().setIdAndRefreshEdges(2L).addTypes(INSTANCE);
  }

  private static ResourceEdge getBookFormatEdge() {
    return new ResourceEdge(getInstance(), new Resource().setIdAndRefreshEdges(3L), BOOK_FORMAT);
  }

  private static ResourceEdge getSerialWorkEdge() {
    return new ResourceEdge(getInstance(), getWork(CONTINUING_RESOURCES), INSTANTIATES);
  }
}
