package org.folio.linked.data.service.validation;

import org.folio.linked.data.exception.ValidationException;
import org.springframework.util.CollectionUtils;

@FunctionalInterface
public interface LdValidator<T> {

  void validate(ValidationContext<T> context);

  default LdValidator<T> andThen(LdValidator<T> after) {
    return (ValidationContext<T> t) -> {
      validate(t);
      after.validate(t);
    };
  }

  default void check(ValidationContext<T> context) {
    validate(context);
    checkErrors(context);
  }

  private void checkErrors(ValidationContext<T> context) {
    var validationErrors = context.getValidationErrors();
    if (!CollectionUtils.isEmpty(validationErrors)) {
      throw new ValidationException("message", "key", "value");
    }
  }
}
