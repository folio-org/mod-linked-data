package org.folio.linked.data.mapper.dto.monograph.work.sub.creator;

import static org.folio.ld.dictionary.PredicateDictionary.CREATOR;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ORGANIZATION;

import org.folio.linked.data.domain.dto.Agent;
import org.folio.linked.data.mapper.dto.common.MapperUnit;
import org.folio.linked.data.mapper.dto.monograph.work.sub.AgentRoleAssigner;
import org.folio.linked.data.service.resource.ResourceMarcAuthorityService;
import org.springframework.stereotype.Component;

@Component("creatorOrganizationMapperUnit")
@MapperUnit(type = ORGANIZATION, requestDto = Agent.class, predicate = CREATOR)
public class OrganizationMapperUnit extends CreatorMapperUnit {

  public OrganizationMapperUnit(AgentRoleAssigner agentRoleAssigner,
                                ResourceMarcAuthorityService resourceMarcAuthorityService) {
    super(agentRoleAssigner, resourceMarcAuthorityService);
  }
}
