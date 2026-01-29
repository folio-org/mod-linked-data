package org.folio.linked.data.mapper.dto.resource.common.agent;

import static org.folio.ld.dictionary.PredicateDictionary.CONTRIBUTOR;
import static org.folio.ld.dictionary.PredicateDictionary.CREATOR;
import static org.folio.ld.dictionary.ResourceTypeDictionary.JURISDICTION;

import org.folio.linked.data.domain.dto.Agent;
import org.folio.linked.data.mapper.dto.resource.base.MapperUnit;
import org.folio.linked.data.service.reference.ReferenceService;
import org.springframework.stereotype.Component;

@Component
@MapperUnit(type = JURISDICTION, requestDto = Agent.class, predicate = {CREATOR, CONTRIBUTOR})
public class JurisdictionMapperUnit extends AgentMapperUnit {

  public JurisdictionMapperUnit(AgentRoleAssigner agentRoleAssigner,
                                ReferenceService referenceService) {
    super(agentRoleAssigner, referenceService);
  }
}
