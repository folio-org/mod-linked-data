package org.folio.linked.data.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.folio.linked.data.validation.entity.ResourceTypeValidator;

@Documented
@Constraint(validatedBy = ResourceTypeValidator.class)
@Target({ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ResourceTypeConstraint {
  String message() default "{resourceTypeConstraint.message}";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
