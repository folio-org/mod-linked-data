package org.folio.linked.data.model.entity.event;

import static java.util.Objects.nonNull;

import org.folio.linked.data.model.entity.Resource;

public record ResourceUpdatedEvent(Resource newWork, Resource oldWork) {

  public boolean isSameResourceUpdated() {
    if (nonNull(newWork) && nonNull(oldWork) && nonNull(newWork.getId())) {
      return newWork.getId().equals(oldWork.getId());
    } else {
      return false;
    }
  }
}
