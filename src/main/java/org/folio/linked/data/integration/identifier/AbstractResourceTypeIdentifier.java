package org.folio.linked.data.integration.identifier;

import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceTypeEntity;

public abstract class AbstractResourceTypeIdentifier implements Predicate<Resource> {

  private final Set<String> types;

  public AbstractResourceTypeIdentifier(Collection<ResourceTypeDictionary> types) {
    this.types = types.stream()
      .map(ResourceTypeDictionary::getUri)
      .collect(Collectors.toSet());
  }

  @Override
  public boolean test(Resource resource) {
    return resource.getTypes()
      .stream()
      .map(ResourceTypeEntity::getUri)
      .anyMatch(types::contains);
  }
}
