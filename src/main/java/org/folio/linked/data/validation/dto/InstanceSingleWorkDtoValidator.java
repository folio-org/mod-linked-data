package org.folio.linked.data.validation.dto;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.List;
import org.folio.linked.data.domain.dto.IdField;
import org.folio.linked.data.validation.InstanceSingleWorkConstraint;

public class InstanceSingleWorkDtoValidator implements
  ConstraintValidator<InstanceSingleWorkConstraint, List<IdField>> {

  @Override
  public boolean isValid(List<IdField> workReferenceFields, ConstraintValidatorContext context) {
    if (isNull(workReferenceFields)) {
      return false;
    }
    return workReferenceFields.stream()
      .filter(wr -> nonNull(wr.getId()))
      .count() == 1;
  }

}
