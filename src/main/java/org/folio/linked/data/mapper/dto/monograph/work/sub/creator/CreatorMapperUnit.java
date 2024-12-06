package org.folio.linked.data.mapper.dto.monograph.work.sub.creator;

import org.folio.linked.data.domain.dto.WorkResponse;
import org.folio.linked.data.mapper.dto.monograph.work.sub.AgentMapperUnit;
import org.folio.linked.data.mapper.dto.monograph.work.sub.AgentRoleAssigner;
import org.folio.linked.data.service.resource.marc.ResourceMarcAuthorityService;

public abstract class CreatorMapperUnit extends AgentMapperUnit {

  CreatorMapperUnit(AgentRoleAssigner agentRoleAssigner,
                    ResourceMarcAuthorityService resourceMarcAuthorityService) {
    super((dto, creator) -> {
      if (dto instanceof WorkResponse work) {
        work.addCreatorReferenceItem(creator);
      }
    }, agentRoleAssigner, resourceMarcAuthorityService);
  }

}
