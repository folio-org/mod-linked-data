package org.folio.linked.data.validation.dto;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.model.CreateProfileSettingsRequest;
import org.folio.linked.data.service.profile.ProfileSettingsService;
import org.folio.linked.data.validation.ProfileSettingsNameUniqueConstraint;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Log4j2
@SuppressWarnings("javaarchitecture:S7091")
public class ProfileSettingsNameUniquenessValidator
  implements ConstraintValidator<ProfileSettingsNameUniqueConstraint, CreateProfileSettingsRequest> {

  private final ProfileSettingsService profileSettingsService;

  @Override
  public boolean isValid(
    CreateProfileSettingsRequest createProfileSettingsRequest, ConstraintValidatorContext context) {
    if (createProfileSettingsRequest.getProfileId() == null
      || createProfileSettingsRequest.getCustomProfileSettingsRequestDto() == null) {
      return true;
    }

    var isDuplicate = profileSettingsService.nameExistsForProfile(
      createProfileSettingsRequest.getProfileId(), createProfileSettingsRequest.getCustomProfileSettingsRequestDto());

    if (isDuplicate.booleanValue()) {
      context.disableDefaultConstraintViolation();
      context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
        .addPropertyNode("customProfileSettingsRequestDto")
        .addPropertyNode("name")
        .addConstraintViolation();
    }

    return !isDuplicate;
  }
}
