package org.folio.linked.data.mapper.dto.monograph.work.sub.creator;

import static org.folio.ld.dictionary.PredicateDictionary.CREATOR;
import static org.folio.ld.dictionary.ResourceTypeDictionary.FAMILY;

import org.folio.linked.data.domain.dto.Agent;
import org.folio.linked.data.mapper.dto.common.MapperUnit;
import org.folio.linked.data.mapper.dto.monograph.work.sub.AgentRoleAssigner;
import org.folio.linked.data.service.resource.marc.ResourceMarcAuthorityService;
import org.springframework.stereotype.Component;

@Component("creatorFamilyMapperUnit")
@MapperUnit(type = FAMILY, requestDto = Agent.class, predicate = CREATOR)
public class FamilyMapperUnit extends CreatorMapperUnit {

  public FamilyMapperUnit(AgentRoleAssigner agentRoleAssigner,
                          ResourceMarcAuthorityService resourceMarcAuthorityService) {
    super(agentRoleAssigner, resourceMarcAuthorityService);
  }
}
