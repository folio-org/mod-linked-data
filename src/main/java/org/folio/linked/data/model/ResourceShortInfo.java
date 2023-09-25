package org.folio.linked.data.model;

import static java.util.Objects.isNull;

import java.util.Set;
import org.folio.linked.data.model.entity.ResourceType;

public interface ResourceShortInfo {

  Long getResourceHash();

  String getLabel();

  Set<ResourceType> getTypes();

  default ResourceType getFirstType() {
    return (isNull(getTypes()) || getTypes().isEmpty()) ? null : getTypes().iterator().next();
  }
}
