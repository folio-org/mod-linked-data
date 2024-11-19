package org.folio.linked.data.validation.dto;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;
import org.folio.linked.data.domain.dto.LccnRequest;
import org.folio.linked.data.validation.LccnPatternConstraint;

public class LccnPatternValidator implements ConstraintValidator<LccnPatternConstraint, LccnRequest> {

  private static final Pattern LCCN_STRUCTURE_A_PATTERN = Pattern.compile("( {3}|[a-z][ |a-z]{2})\\d{8} ");
  private static final Pattern LCCN_STRUCTURE_B_PATTERN = Pattern.compile("( {2}|[a-z][ |a-z])\\d{10}");

  @Override
  public boolean isValid(LccnRequest lccnRequest, ConstraintValidatorContext constraintValidatorContext) {
    if (isCurrent(lccnRequest)) {
      return lccnRequest.getValue()
        .stream()
        .anyMatch(this::hasValidPattern);
    } else {
      return true;
    }
  }

  private boolean isCurrent(LccnRequest lccnRequest) {
    return isEmpty(lccnRequest.getStatus()) || lccnRequest.getStatus()
      .stream()
      .flatMap(status -> status.getLink().stream())
      .anyMatch(link -> link.endsWith("current"));
  }

  private boolean hasValidPattern(String lccnValue) {
    return LCCN_STRUCTURE_A_PATTERN.matcher(lccnValue).matches()
      || LCCN_STRUCTURE_B_PATTERN.matcher(lccnValue).matches();
  }
}
