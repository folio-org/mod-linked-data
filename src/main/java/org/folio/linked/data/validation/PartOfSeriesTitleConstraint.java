package org.folio.linked.data.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.folio.linked.data.validation.dto.PartOfSeriesTitleDtoValidator;

@Documented
@Constraint(validatedBy = PartOfSeriesTitleDtoValidator.class)
@Target({ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@SuppressWarnings("javaarchitecture:S7091")
public @interface PartOfSeriesTitleConstraint {

  String message() default "{partOfSeriesTitleConstraint.message}";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

}
