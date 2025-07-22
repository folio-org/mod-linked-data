package org.folio.linked.data.validation.dto;

import static java.util.Objects.isNull;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.folio.linked.data.domain.dto.PartOfSeries;
import org.folio.linked.data.validation.PartOfSeriesTitleConstraint;

@RequiredArgsConstructor
@SuppressWarnings("javaarchitecture:S7091")
public class PartOfSeriesTitleDtoValidator implements
  ConstraintValidator<PartOfSeriesTitleConstraint, List<PartOfSeries>> {

  @Override
  public boolean isValid(List<PartOfSeries> series, ConstraintValidatorContext context) {
    if (isNull(series)) {
      return true;
    }

    return series.stream()
      .allMatch(this::titleIsNotEmpty);
  }

  private boolean titleIsNotEmpty(PartOfSeries series) {
    return !series.getName().isEmpty()
            && series.getName().stream().anyMatch(StringUtils::isNotBlank);
  }
}
