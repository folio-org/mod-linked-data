package org.folio.linked.data.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.folio.linked.data.validation.dto.LccnUniquenessValidator;

@Documented
@SuppressWarnings("javaarchitecture:S7091")
@Constraint(validatedBy = LccnUniquenessValidator.class)
@Target({ElementType.FIELD, ElementType.TYPE, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface LccnUniqueConstraint {

  String message() default "{lccnUniqueConstraint.message}";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
