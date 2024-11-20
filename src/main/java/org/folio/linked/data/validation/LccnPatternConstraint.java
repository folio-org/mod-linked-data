package org.folio.linked.data.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.folio.linked.data.validation.dto.LccnPatternValidator;

@Documented
@Constraint(validatedBy = LccnPatternValidator.class)
@Target({ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface LccnPatternConstraint {

  String message() default "{lccnPatternConstraint.message}";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
