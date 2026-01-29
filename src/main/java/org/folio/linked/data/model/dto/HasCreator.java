package org.folio.linked.data.model.dto;

import org.folio.linked.data.domain.dto.AgentResponse;

public interface HasCreator {
  HasCreator addCreatorReferenceItem(AgentResponse agent);

  HasCreator addContributorReferenceItem(AgentResponse agent);
}
