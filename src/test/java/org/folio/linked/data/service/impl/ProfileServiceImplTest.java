package org.folio.linked.data.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.linked.data.util.Constants.PROFILE_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.folio.linked.data.exception.NotFoundException;
import org.folio.linked.data.model.entity.Profile;
import org.folio.linked.data.repo.ProfileRepository;
import org.folio.spring.test.type.UnitTest;
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

  @Test
  void getProfile_shouldReturnProfile() {
    //given
    var id = 1;
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
    when(profileRepository.findById(1)).thenReturn(Optional.empty());

    //when
    var thrown = assertThrows(
      NotFoundException.class,
      () -> profileService.getProfile()
    );

    //then
    assertThat(thrown.getClass()).isEqualTo(NotFoundException.class);
    assertThat(thrown.getMessage()).isEqualTo(PROFILE_NOT_FOUND);
  }
}
