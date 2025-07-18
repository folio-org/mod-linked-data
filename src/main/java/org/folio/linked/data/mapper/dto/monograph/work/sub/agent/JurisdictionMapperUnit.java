package org.folio.linked.data.mapper.dto.monograph.work.sub.agent;

import static org.folio.ld.dictionary.PredicateDictionary.CONTRIBUTOR;
import static org.folio.ld.dictionary.PredicateDictionary.CREATOR;
import static org.folio.ld.dictionary.ResourceTypeDictionary.JURISDICTION;

import org.folio.linked.data.domain.dto.Agent;
import org.folio.linked.data.mapper.dto.common.MapperUnit;
import org.folio.linked.data.service.resource.marc.ResourceMarcAuthorityService;
import org.springframework.stereotype.Component;

@Component
@MapperUnit(type = JURISDICTION, requestDto = Agent.class, predicate = {CREATOR, CONTRIBUTOR})
public class JurisdictionMapperUnit extends AgentMapperUnit {

  public JurisdictionMapperUnit(AgentRoleAssigner agentRoleAssigner,
                                ResourceMarcAuthorityService resourceMarcAuthorityService) {
    super(agentRoleAssigner, resourceMarcAuthorityService);
  }
}
