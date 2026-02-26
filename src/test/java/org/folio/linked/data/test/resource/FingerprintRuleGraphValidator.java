package org.folio.linked.data.test.resource;

import static java.util.function.Predicate.not;
import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.ResourceTypeDictionary.BOOKS;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.ld.fingerprint.config.FingerprintRules;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.model.entity.ResourceTypeEntity;
import org.springframework.stereotype.Service;

@Service
public class FingerprintRuleGraphValidator {

  private static final Set<Set<String>> EXCLUDED_TYPE_COMBINATIONS = Set.of(
    Set.of(WORK.name(), BOOKS.name()) // covered by partialTypeMatch rule in fingerprint library
  );

  private final Set<Set<String>> fingerprintTypeCombinations;

  public FingerprintRuleGraphValidator(FingerprintRules fingerprintRules) {
    this.fingerprintTypeCombinations = fingerprintRules.getRules().stream()
      .map(FingerprintRules.FingerprintRule::types)
      .map(this::normalizeTypes)
      .collect(Collectors.toCollection(LinkedHashSet::new));
  }

  public void validateFingerprintRuleExists(Resource rootResource) {
    validateFingerprintRuleExists(rootResource, Set.of());
  }

  public void validateFingerprintRuleExists(Resource rootResource, Set<Set<String>> excludedTypes) {
    var excludedTypeCombinations = new LinkedHashSet<>(EXCLUDED_TYPE_COMBINATIONS);
    excludedTypeCombinations.addAll(excludedTypes);

    var resourcesWithMissingRules = collectOutgoingResources(rootResource).stream()
      .map(this::toNormalizedTypeCombination)
      .filter(not(excludedTypeCombinations::contains))
      .filter(not(fingerprintTypeCombinations::contains))
      .collect(Collectors.toCollection(LinkedHashSet::new));

    assertThat(resourcesWithMissingRules)
      .withFailMessage(() -> "FingerprintRules does not contain type combinations: " + resourcesWithMissingRules)
      .isEmpty();
  }

  private Set<Resource> collectOutgoingResources(Resource rootResource) {
    var visitedResourceIds = new LinkedHashSet<Long>();
    var resources = new LinkedHashSet<Resource>();
    collectOutgoingResources(rootResource, visitedResourceIds, resources);

    return resources;
  }

  private void collectOutgoingResources(Resource resource, Set<Long> visitedResourceIds, Set<Resource> resources) {
    if (resource == null || !visitedResourceIds.add(resource.getId())) {
      return;
    }

    resources.add(resource);
    resource.getOutgoingEdges().stream()
      .map(ResourceEdge::getTarget)
      .filter(Objects::nonNull)
      .forEach(target -> collectOutgoingResources(target, visitedResourceIds, resources));
  }

  private Set<String> toNormalizedTypeCombination(Resource resource) {
    return normalizeTypes(resource.getTypes().stream()
      .map(ResourceTypeEntity::getUri)
      .collect(Collectors.toCollection(LinkedHashSet::new)));
  }

  private Set<String> normalizeTypes(Collection<?> types) {
    return types.stream()
      .filter(Objects::nonNull)
      .map(this::toTypeName)
      .map(String::trim)
      .filter(not(String::isBlank))
      .collect(Collectors.toCollection(LinkedHashSet::new));
  }

  private String toTypeName(Object type) {
    if (type instanceof Enum<?> enumType) {
      return enumType.name();
    }

    var typeName = String.valueOf(type);
    return ResourceTypeDictionary.fromUri(typeName)
      .map(Enum::name)
      .orElse(typeName);
  }
}
