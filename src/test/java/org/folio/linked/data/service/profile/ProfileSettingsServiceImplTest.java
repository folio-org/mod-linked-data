package org.folio.linked.data.service.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.linked.data.test.TestUtil.emptyRequestProcessingException;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;
import org.folio.linked.data.domain.dto.CustomProfileSettingsRequestDto;
import org.folio.linked.data.exception.RequestProcessingException;
import org.folio.linked.data.exception.RequestProcessingExceptionBuilder;
import org.folio.linked.data.model.entity.Profile;
import org.folio.linked.data.repo.ProfileRepository;
import org.folio.linked.data.repo.ProfileSettingsRepository;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class ProfileSettingsServiceImplTest {

  @InjectMocks
  private ProfileSettingsServiceImpl profileSettingsService;
  @Mock
  private ProfileSettingsRepository profileSettingsRepository;
  @Mock
  private ProfileRepository profileRepository;
  @Mock
  private RequestProcessingExceptionBuilder exceptionBuilder;
  @Mock
  private FolioExecutionContext executionContext;

  @Test
  void getAllProfileSettings_shouldReturnEmptyList_ifNoSettings() {
    // given
    var userId = UUID.randomUUID();
    doReturn(userId).when(executionContext).getUserId();
    var id = 1;
    var profile = new Profile();
    profile.setId(id);
    when(profileRepository.findById(id)).thenReturn(Optional.of(profile));

    // when
    var settings = profileSettingsService.getAllProfileSettings(id);

    // then
    assertThat(settings).isEmpty();
  }

  @Test
  void getAllProfileSettings_shouldThrowNotFound_ifNoSuchProfile() {
    // given
    var userId = UUID.randomUUID();
    doReturn(userId).when(executionContext).getUserId();
    var id = 1;
    when(profileRepository.findById(id)).thenReturn(Optional.empty());
    when(exceptionBuilder.notFoundLdResourceByIdException(anyString(), anyString()))
      .thenReturn(emptyRequestProcessingException());

    // when
    var thrown = assertThrows(RequestProcessingException.class,
      () -> profileSettingsService.getAllProfileSettings(id));

    // then
    assertThat(thrown.getClass()).isEqualTo(RequestProcessingException.class);
    assertThat(thrown.getMessage()).isEmpty();
  }

  @Test
  void getProfileSettings_shouldThrowNotFound_ifNoSuchProfile() {
    // given
    var userId = UUID.randomUUID();
    doReturn(userId).when(executionContext).getUserId();
    var id = 1;
    var settingsId = 1;
    when(profileRepository.findById(id)).thenReturn(Optional.empty());
    when(exceptionBuilder.notFoundLdResourceByIdException(anyString(), anyString()))
      .thenReturn(emptyRequestProcessingException());

    // when
    var thrown = assertThrows(RequestProcessingException.class,
      () -> profileSettingsService.getProfileSettings(id, settingsId));

    // then
    assertThat(thrown.getClass()).isEqualTo(RequestProcessingException.class);
    assertThat(thrown.getMessage()).isEmpty();
  }

  @Test
  void getProfileSettings_shouldReturnInactiveSettings_ifNoSuchSettings() {
    // given
    var userId = UUID.randomUUID();
    doReturn(userId).when(executionContext).getUserId();
    var id = 1;
    var profile = new Profile();
    profile.setId(id);
    when(profileRepository.findById(id)).thenReturn(Optional.of(profile));
    var profileSettingsId = 2;

    // when
    var settings = profileSettingsService.getProfileSettings(id, profileSettingsId);

    // then
    assertThat(settings.getActive()).isFalse();
    assertThat(settings.getChildren()).isNull();
  }

  @Test
  void createProfileSettings_shouldThrowNotFound_ifNoSuchProfile() {
    // given
    var userId = UUID.randomUUID();
    doReturn(userId).when(executionContext).getUserId();
    var id = 1;
    var name = "name";
    when(profileRepository.findById(id)).thenReturn(Optional.empty());
    when(exceptionBuilder.notFoundLdResourceByIdException(anyString(), anyString()))
      .thenReturn(emptyRequestProcessingException());
    var settings = new CustomProfileSettingsRequestDto(name, false, null);

    // when
    var thrown = assertThrows(RequestProcessingException.class,
      () -> profileSettingsService.createProfileSettings(id, settings));

    // then
    assertThat(thrown.getClass()).isEqualTo(RequestProcessingException.class);
    assertThat(thrown.getMessage()).isEmpty();
  }
  
  @Test
  void setProfileSettings_shouldThrowNotFound_ifNoSuchProfile() {
    // given
    var userId = UUID.randomUUID();
    doReturn(userId).when(executionContext).getUserId();
    var id = 1;
    var name = "name";
    when(profileRepository.findById(id)).thenReturn(Optional.empty());
    when(exceptionBuilder.notFoundLdResourceByIdException(anyString(), anyString()))
      .thenReturn(emptyRequestProcessingException());
    var profileSettingsId = 5;
    var settings = new CustomProfileSettingsRequestDto(name, false, null);

    // when
    var thrown = assertThrows(RequestProcessingException.class,
      () -> profileSettingsService.setProfileSettings(id, profileSettingsId, settings));

    // then
    assertThat(thrown.getClass()).isEqualTo(RequestProcessingException.class);
    assertThat(thrown.getMessage()).isEmpty();
  }

  @Test
  void deleteProfileSettings_shouldThrowNotFound_ifNoSuchProfile() {
    // given
    var userId = UUID.randomUUID();
    doReturn(userId).when(executionContext).getUserId();
    var id = 1;
    when(profileRepository.findById(id)).thenReturn(Optional.empty());
    when(exceptionBuilder.notFoundLdResourceByIdException(anyString(), anyString()))
      .thenReturn(emptyRequestProcessingException());
    var profileSettingsId = 5;

    // when
    var thrown = assertThrows(RequestProcessingException.class,
      () -> profileSettingsService.deleteProfileSettings(id, profileSettingsId));

    // then
    assertThat(thrown.getClass()).isEqualTo(RequestProcessingException.class);
    assertThat(thrown.getMessage()).isEmpty();
  }
}
