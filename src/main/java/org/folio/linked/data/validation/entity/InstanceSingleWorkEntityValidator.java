package org.folio.linked.data.validation.entity;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.folio.ld.dictionary.PredicateDictionary.INSTANTIATES;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.LIGHT_RESOURCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.validation.InstanceSingleWorkConstraint;

public class InstanceSingleWorkEntityValidator implements ConstraintValidator<InstanceSingleWorkConstraint, Resource> {

  @Override
  public boolean isValid(Resource resource, ConstraintValidatorContext context) {
    if (resource.isNotOfType(INSTANCE) || resource.isOfType(LIGHT_RESOURCE)) {
      return true;
    }
    if (isEmpty(resource.getOutgoingEdges())) {
      return false;
    }
    return resource.getOutgoingEdges().stream()
      .filter(edge -> edge.getPredicate().getUri().equals(INSTANTIATES.getUri()))
      .map(ResourceEdge::getTarget)
      .filter(target -> target.isOfType(WORK))
      .count() == 1;
  }

}
