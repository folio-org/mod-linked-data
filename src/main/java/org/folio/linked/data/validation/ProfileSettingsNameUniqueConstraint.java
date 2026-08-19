package org.folio.linked.data.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.folio.linked.data.validation.dto.ProfileSettingsNameUniquenessValidator;

@Documented
@SuppressWarnings("javaarchitecture:S7091")
@Constraint(validatedBy = ProfileSettingsNameUniquenessValidator.class)
@Target({ElementType.FIELD, ElementType.TYPE, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ProfileSettingsNameUniqueConstraint {

  String message() default "{profileSettingsNameUniqueConstraint.message}";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

}
