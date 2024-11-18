package org.folio.linked.data.service.validation.validators;

import java.util.regex.Pattern;
import org.folio.linked.data.domain.dto.InstanceField;
import org.folio.linked.data.domain.dto.LccnField;
import org.folio.linked.data.domain.dto.LccnRequest;
import org.folio.linked.data.domain.dto.ResourceRequestDto;
import org.folio.linked.data.exception.ValidationException;
import org.folio.linked.data.service.validation.LdValidator;
import org.folio.linked.data.service.validation.ValidationContext;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Qualifier(LccnPatternValidator.CODE)
public class LccnPatternValidator implements LdValidator<ResourceRequestDto> {

  public static final String CODE = "invalidLccnSubfieldValue";

  private static final Pattern LCCN_STRUCTURE_A_PATTERN = Pattern.compile("( {3}|[a-z][ |a-z]{2})\\d{8} ");
  private static final Pattern LCCN_STRUCTURE_B_PATTERN = Pattern.compile("( {2}|[a-z][ |a-z])\\d{10}");

  @Override
  public void validate(ValidationContext<ResourceRequestDto> context) {
    var requestDto = context.getObjectToValidate();
    var resource = requestDto.getResource();
    if (resource instanceof InstanceField instanceField && hasInvalidLccn(instanceField)) {
      // after implementing Error Messaging Framework we can add validation error to ValidationContext
      throw new ValidationException("Invalid LCCN", "key", "value");
    }
  }

  private boolean hasInvalidLccn(InstanceField instanceField) {
    return instanceField.getInstance()
      .getMap()
      .stream()
      .filter(LccnField.class::isInstance)
      .map(i -> (LccnField) i)
      .map(LccnField::getLccn)
      .filter(this::isCurrent)
      .flatMap(lccnRequest -> lccnRequest.getValue().stream())
      .anyMatch(this::hasInvalidPattern);
  }

  private boolean isCurrent(LccnRequest lccnRequest) {
    return lccnRequest.getStatus()
      .stream()
      .flatMap(status -> status.getLink().stream())
      .anyMatch(link -> link.endsWith("current"));
  }

  private boolean hasInvalidPattern(String lccnValue) {
    return !LCCN_STRUCTURE_A_PATTERN.matcher(lccnValue).matches()
      && !LCCN_STRUCTURE_B_PATTERN.matcher(lccnValue).matches();
  }
}
