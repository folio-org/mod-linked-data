package org.folio.linked.data.validation.entity;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.folio.ld.dictionary.ResourceTypeDictionary.MOCKED_RESOURCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.validation.ResourceTypeConstraint;

public class ResourceTypeValidator implements ConstraintValidator<ResourceTypeConstraint, Resource> {
  @Override
  public boolean isValid(Resource resource, ConstraintValidatorContext constraintValidatorContext) {
    return isNotEmpty(resource.getTypes())
      && resource.isNotOfType(MOCKED_RESOURCE)
      && isValidWork(resource);
  }

  private boolean isValidWork(Resource resource) {
    return resource.isNotOfType(WORK) || resource.getTypes().size() > 1;
  }
}
