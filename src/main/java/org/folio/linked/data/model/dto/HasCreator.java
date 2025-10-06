package org.folio.linked.data.model.dto;

import org.folio.linked.data.domain.dto.Agent;

public interface HasCreator {
  HasCreator addCreatorReferenceItem(Agent agent);

  HasCreator addContributorReferenceItem(Agent agent);
}
