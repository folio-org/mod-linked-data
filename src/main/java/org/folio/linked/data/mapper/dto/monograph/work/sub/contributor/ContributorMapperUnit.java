package org.folio.linked.data.mapper.dto.monograph.work.sub.contributor;

import org.folio.linked.data.domain.dto.WorkResponse;
import org.folio.linked.data.mapper.dto.monograph.work.sub.AgentMapperUnit;
import org.folio.linked.data.mapper.dto.monograph.work.sub.AgentRoleAssigner;
import org.folio.linked.data.repo.ResourceRepository;

public class ContributorMapperUnit extends AgentMapperUnit {

  ContributorMapperUnit(AgentRoleAssigner agentRoleAssigner, ResourceRepository resourceRepository) {
    super((dto, creator) -> {
      if (dto instanceof WorkResponse work) {
        work.addContributorReferenceItem(creator);
      }
    }, agentRoleAssigner, resourceRepository);
  }
}
