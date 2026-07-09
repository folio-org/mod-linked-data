package org.folio.linked.data.validation.entity;

import static java.util.Objects.isNull;
import static org.folio.ld.dictionary.PropertyDictionary.NAME;
import static org.folio.ld.dictionary.ResourceTypeDictionary.CONCEPT;
import static org.folio.ld.dictionary.ResourceTypeDictionary.HUB;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.validation.AuthorityNameConstraint;

public class AuthorityNameEntityValidator implements ConstraintValidator<AuthorityNameConstraint, Resource> {

  @Override
  public boolean isValid(Resource resource, ConstraintValidatorContext context) {
    if (!resource.isAuthority() || resource.isOfType(HUB) || resource.isOfType(CONCEPT)) {
      return true;
    }
    var doc = resource.getDoc();
    if (isNull(doc) || !doc.has(NAME.getValue())) {
      return false;
    }
    return !doc.get(NAME.getValue()).isEmpty();
  }
}
