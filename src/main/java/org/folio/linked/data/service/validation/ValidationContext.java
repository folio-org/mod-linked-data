package org.folio.linked.data.service.validation;

import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ValidationContext<T> {

  private final T objectToValidate;
  private final Set<ValidationError> validationErrors = new HashSet<>();

  public void addValidationError(ValidationError error) {
    validationErrors.add(error);
  }
}
