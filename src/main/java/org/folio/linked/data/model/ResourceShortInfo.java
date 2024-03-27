package org.folio.linked.data.model;

import static java.util.Objects.isNull;

import java.util.Set;
import org.folio.linked.data.model.entity.ResourceTypeEntity;

public interface ResourceShortInfo {

  Long getId();

  String getLabel();

  Set<ResourceTypeEntity> getTypes();

  default ResourceTypeEntity getFirstType() {
    return (isNull(getTypes()) || getTypes().isEmpty()) ? null : getTypes().iterator().next();
  }
}
