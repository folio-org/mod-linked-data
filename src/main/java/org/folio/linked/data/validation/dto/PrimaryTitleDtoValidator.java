package org.folio.linked.data.validation.dto;

import static java.util.Optional.ofNullable;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.folio.linked.data.domain.dto.PrimaryTitleField;
import org.folio.linked.data.domain.dto.TitleField;
import org.folio.linked.data.validation.PrimaryTitleConstraint;

public class PrimaryTitleDtoValidator implements ConstraintValidator<PrimaryTitleConstraint, List<TitleField>> {

  @Override
  public boolean isValid(List<TitleField> titleFields, ConstraintValidatorContext context) {
    return ofNullable(titleFields)
      .map(tfs -> tfs.stream()
        .filter(PrimaryTitleField.class::isInstance)
        .map(ptf -> ((PrimaryTitleField) ptf).getPrimaryTitle())
        .filter(pt -> isNotEmpty(pt.getMainTitle()))
        .flatMap(pt -> pt.getMainTitle().stream())
        .anyMatch(StringUtils::isNotBlank))
      .orElse(false);
  }

}
