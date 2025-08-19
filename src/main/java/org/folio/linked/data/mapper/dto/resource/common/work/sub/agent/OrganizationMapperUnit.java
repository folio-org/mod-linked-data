package org.folio.linked.data.mapper.dto.resource.common.work.sub.agent;

import static org.folio.ld.dictionary.PredicateDictionary.CONTRIBUTOR;
import static org.folio.ld.dictionary.PredicateDictionary.CREATOR;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ORGANIZATION;

import org.folio.linked.data.domain.dto.Agent;
import org.folio.linked.data.mapper.dto.resource.base.MapperUnit;
import org.folio.linked.data.service.resource.marc.ResourceMarcAuthorityService;
import org.springframework.stereotype.Component;

@Component
@MapperUnit(type = ORGANIZATION, requestDto = Agent.class, predicate = {CREATOR, CONTRIBUTOR})
public class OrganizationMapperUnit extends AgentMapperUnit {

  public OrganizationMapperUnit(AgentRoleAssigner agentRoleAssigner,
                                ResourceMarcAuthorityService resourceMarcAuthorityService) {
    super(agentRoleAssigner, resourceMarcAuthorityService);
  }
}
