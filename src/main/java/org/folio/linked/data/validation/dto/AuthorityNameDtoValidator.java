package org.folio.linked.data.validation.dto;


import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.folio.linked.data.validation.AuthorityNameConstraint;

public class AuthorityNameDtoValidator implements ConstraintValidator<AuthorityNameConstraint, List<String>> {

  @Override
  public boolean isValid(List<String> name, ConstraintValidatorContext context) {
    if (CollectionUtils.isEmpty(name)) {
      return false;
    }
    return name.stream().anyMatch(StringUtils::isNotBlank);
  }
}
