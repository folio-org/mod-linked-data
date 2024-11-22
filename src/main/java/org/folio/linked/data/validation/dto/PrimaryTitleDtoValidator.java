package org.folio.linked.data.validation.dto;

import static java.util.Objects.isNull;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.folio.linked.data.domain.dto.PrimaryTitleField;
import org.folio.linked.data.domain.dto.TitleFieldRequestTitleInner;
import org.folio.linked.data.validation.PrimaryTitleConstraint;

public class PrimaryTitleDtoValidator implements
  ConstraintValidator<PrimaryTitleConstraint, List<TitleFieldRequestTitleInner>> {

  @Override
  public boolean isValid(List<TitleFieldRequestTitleInner> titleFields, ConstraintValidatorContext context) {
    if (isNull(titleFields)) {
      return true;
    }
    return titleFields.stream()
      .filter(PrimaryTitleField.class::isInstance)
      .map(ptf -> ((PrimaryTitleField) ptf).getPrimaryTitle())
      .filter(pt -> isNotEmpty(pt.getMainTitle()))
      .flatMap(pt -> pt.getMainTitle().stream())
      .anyMatch(StringUtils::isNotBlank);
  }

}
