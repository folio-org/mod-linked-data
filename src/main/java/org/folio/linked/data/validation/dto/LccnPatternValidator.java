package org.folio.linked.data.validation.dto;

import static org.folio.linked.data.util.LccnUtils.isCurrent;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.LccnRequest;
import org.folio.linked.data.validation.LccnPatternConstraint;
import org.folio.linked.data.validation.spec.SpecProvider;
import org.folio.rspec.domain.dto.SpecificationRuleDto;

@RequiredArgsConstructor
@SuppressWarnings("javaarchitecture:S7091")
public class LccnPatternValidator implements ConstraintValidator<LccnPatternConstraint, LccnRequest> {

  public static final String CODE = "invalidLccnSubfieldValue";

  private static final Pattern LCCN_STRUCTURE_A_PATTERN = Pattern.compile("( {3}|[a-z][ a-z]{2})\\d{8} ");
  private static final Pattern LCCN_STRUCTURE_B_PATTERN = Pattern.compile("( {2}|[a-z][ a-z])\\d{10}");

  private final SpecProvider specProvider;
  private final List<Function<String, String>> lccnNormalizers;

  @Override
  public boolean isValid(LccnRequest lccnRequest, ConstraintValidatorContext constraintValidatorContext) {

    if (isCurrent(lccnRequest) && isLccnFormatValidationEnabled()) {
      normalize(lccnRequest);
      return lccnRequest.getValue()
        .stream()
        .anyMatch(this::hasValidPattern);
    } else {
      return true;
    }
  }

  private boolean isLccnFormatValidationEnabled() {
    return specProvider.getSpecRules()
      .stream()
      .filter(rule -> CODE.equals(rule.getCode()))
      .findFirst()
      .map(SpecificationRuleDto::getEnabled)
      .orElse(false);
  }

  private void normalize(LccnRequest lccnRequest) {
    var normalizer = lccnNormalizers.stream()
      .reduce(Function.identity(), Function::andThen);
    var normalized = lccnRequest.getValue()
      .stream()
      .map(normalizer)
      .toList();
    lccnRequest.setValue(normalized);
  }

  private boolean hasValidPattern(String lccnValue) {
    return LCCN_STRUCTURE_A_PATTERN.matcher(lccnValue).matches()
      || LCCN_STRUCTURE_B_PATTERN.matcher(lccnValue).matches();
  }
}
