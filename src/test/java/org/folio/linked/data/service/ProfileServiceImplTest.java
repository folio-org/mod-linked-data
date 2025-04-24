package org.folio.linked.data.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.linked.data.test.TestUtil.emptyRequestProcessingException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.folio.linked.data.exception.RequestProcessingException;
import org.folio.linked.data.exception.RequestProcessingExceptionBuilder;
import org.folio.linked.data.model.entity.Profile;
import org.folio.linked.data.repo.ProfileRepository;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class ProfileServiceImplTest {

  @InjectMocks
  private ProfileServiceImpl profileService;
  @Mock
  private ProfileRepository profileRepository;
  @Mock
  private RequestProcessingExceptionBuilder exceptionBuilder;

  @Test
  void getProfile_shouldReturnProfile() {
    //given
    var id = "2";
    var value = "[{\"key\": \"value\"}]";
    var profile = new Profile();
    profile.setId(id);
    profile.setValue(value);

    when(profileRepository.findById(id)).thenReturn(Optional.of(profile));

    //when
    var result = profileService.getProfile();

    //then
    assertEquals(value, result);
  }

  @Test
  void getProfile_shouldThrowNotFoundException_ifNoProfileExists() {
    //given
    when(profileRepository.findById("2")).thenReturn(Optional.empty());
    when(exceptionBuilder.notFoundLdResourceByIdException(anyString(), anyString()))
      .thenReturn(emptyRequestProcessingException());

    //when
    var thrown = assertThrows(
      RequestProcessingException.class,
      () -> profileService.getProfile()
    );

    //then
    assertThat(thrown.getClass()).isEqualTo(RequestProcessingException.class);
    assertThat(thrown.getMessage()).isEmpty();
  }

  @Test
  void getProfileById_shouldReturnProfileWithSpecifiedId() {
    //given
    var id = "monogarph-1.0";
    var value = "[{\"key\": \"value\"}]";
    var profile = new Profile();
    profile.setId(id);
    profile.setValue(value);

    when(profileRepository.findById(id)).thenReturn(Optional.of(profile));

    //when
    var result = profileService.getProfileById(id);

    //then
    assertEquals(value, result);
  }

  @Test
  void getProfileById_shouldThrowNotFoundException_ifNoProfileExistsWithSpecifiedId() {
    //given
    var id = "rare-book-2.0";
    when(profileRepository.findById(id)).thenReturn(Optional.empty());
    when(exceptionBuilder.notFoundLdResourceByIdException(anyString(), anyString()))
      .thenReturn(emptyRequestProcessingException());

    //when
    var thrown = assertThrows(
      RequestProcessingException.class,
      () -> profileService.getProfileById(id)
    );

    //then
    assertThat(thrown.getClass()).isEqualTo(RequestProcessingException.class);
    assertThat(thrown.getMessage()).isEmpty();
  }
}
