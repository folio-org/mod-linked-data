package org.folio.linked.data.validation.dto;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintValidatorContext.ConstraintViolationBuilder;
import java.util.List;
import org.folio.linked.data.domain.dto.CustomProfileSettingsRequestDto;
import org.folio.linked.data.model.CreateProfileSettingsRequest;
import org.folio.linked.data.service.profile.ProfileSettingsService;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class ProfileSettingsNameUniquenessValidatorTest {

  private static final String SETTINGS_NAME = "My settings";

  @Mock
  private ProfileSettingsService profileSettingsService;

  @Mock
  private ConstraintValidatorContext context;

  @Mock
  private ConstraintViolationBuilder builder;

  @Mock
  private ConstraintViolationBuilder.NodeBuilderCustomizableContext nodeBuilder;

  @InjectMocks
  private ProfileSettingsNameUniquenessValidator validator;

  @Test
  void shouldReturnTrue_ifNameIsUniqueForProfile() {
    when(profileSettingsService.nameExistsForProfile(any(), any())).thenReturn(false);

    assertTrue(validator.isValid(newCreateProfileSettingsRequest(1, SETTINGS_NAME, true), context));
    verifyNoInteractions(context);
  }

  @Test
  void shouldReturnTrue_ifMissingProfileId() {
    assertTrue(validator.isValid(newCreateProfileSettingsRequest(null, SETTINGS_NAME, true), context));
    verifyNoInteractions(context);
  }

  @Test
  void shouldReturnTrue_ifMissingDto() {
    assertTrue(validator.isValid(newCreateProfileSettingsRequest(1, SETTINGS_NAME, false), context));
    verifyNoInteractions(context);
  }

  @Test
  void shouldReturnFalse_ifNameIsNotUniqueForProfile() {
    when(profileSettingsService.nameExistsForProfile(any(), any())).thenReturn(true);
    when(context.buildConstraintViolationWithTemplate(any())).thenReturn(builder);
    when(builder.addPropertyNode(any())).thenReturn(nodeBuilder);
    when(nodeBuilder.addPropertyNode(any())).thenReturn(nodeBuilder);

    assertFalse(validator.isValid(newCreateProfileSettingsRequest(1, SETTINGS_NAME, true), context));
    verify(context, times(1)).disableDefaultConstraintViolation();
    verify(context, times(1)).buildConstraintViolationWithTemplate(any());
    verify(nodeBuilder, times(1)).addConstraintViolation();
  }

  private CreateProfileSettingsRequest newCreateProfileSettingsRequest(Integer profileId, String name, boolean hasDto) {
    CustomProfileSettingsRequestDto dto = null;
    if (hasDto) {
      dto = new CustomProfileSettingsRequestDto(name, true, List.of());
    }
    return new CreateProfileSettingsRequest(profileId, dto);
  }
}
