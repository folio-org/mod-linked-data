package org.folio.linked.data.validation.entity;

import static java.util.Objects.nonNull;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.folio.ld.dictionary.PropertyDictionary.MAIN_TITLE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.SERIES;
import static org.folio.ld.dictionary.ResourceTypeDictionary.TITLE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.validation.PrimaryTitleConstraint;
import tools.jackson.databind.JsonNode;

public class PrimaryTitleEntityValidator implements ConstraintValidator<PrimaryTitleConstraint, Resource> {

  @Override
  public boolean isValid(Resource resource, ConstraintValidatorContext context) {
    if (isNotWorkOrInstance(resource) || isSeries(resource)) {
      return true;
    }
    if (isEmpty(resource.getOutgoingEdges())) {
      return false;
    }
    return resource.getOutgoingEdges().stream()
      .filter(edge -> edge.getPredicate().getUri().equals(PredicateDictionary.TITLE.getUri()))
      .map(ResourceEdge::getTarget)
      .filter(target -> target.isOfType(TITLE) && nonNull(target.getDoc()))
      .map(Resource::getDoc)
      .anyMatch(this::containsMainTitle);
  }

  private boolean containsMainTitle(JsonNode doc) {
    return doc.has(MAIN_TITLE.getValue()) && !doc.get(MAIN_TITLE.getValue()).isEmpty();
  }

  private boolean isNotWorkOrInstance(Resource resource) {
    return resource.isNotOfType(INSTANCE) && resource.isNotOfType(WORK);
  }

  private boolean isSeries(Resource resource) {
    return resource.isOfType(SERIES);
  }
}
