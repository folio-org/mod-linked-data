package org.folio.linked.data.mapper.dto.monograph.work.sub.creator;

import org.folio.linked.data.domain.dto.Work;
import org.folio.linked.data.domain.dto.WorkReference;
import org.folio.linked.data.mapper.dto.monograph.work.sub.AgentMapperUnit;
import org.folio.linked.data.mapper.dto.monograph.work.sub.AgentRoleAssigner;
import org.folio.linked.data.repo.ResourceRepository;

public abstract class CreatorMapperUnit extends AgentMapperUnit {

  CreatorMapperUnit(AgentRoleAssigner agentRoleAssigner, ResourceRepository resourceRepository) {
    super((dto, creator) -> {
      if (dto instanceof Work work) {
        work.addCreatorReferenceItem(creator);
      }
      if (dto instanceof WorkReference work) {
        work.addCreatorReferenceItem(creator);
      }
    }, agentRoleAssigner, resourceRepository);
  }
}
