package org.folio.linked.data.model;

import org.folio.linked.data.model.entity.ResourceType;

public interface ResourceShortInfo {

  Long getResourceHash();

  String getLabel();

  ResourceType getType();

}
