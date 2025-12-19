package org.folio.linked.data.validation.entity;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.validation.ResourceTypeConstraint;

public class ResourceTypeValidator implements ConstraintValidator<ResourceTypeConstraint, Resource> {
  @Override
  public boolean isValid(Resource resource, ConstraintValidatorContext constraintValidatorContext) {
    return isNotEmpty(resource.getTypes());
  }
}
