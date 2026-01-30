package org.folio.linked.data.mapper.dto.resource.common.agent;

import static org.folio.ld.dictionary.PredicateDictionary.CONTRIBUTOR;
import static org.folio.ld.dictionary.PredicateDictionary.CREATOR;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PERSON;

import org.folio.linked.data.domain.dto.Agent;
import org.folio.linked.data.mapper.dto.resource.base.MapperUnit;
import org.folio.linked.data.service.reference.ReferenceService;
import org.springframework.stereotype.Component;

@Component
@MapperUnit(type = PERSON, requestDto = Agent.class, predicate = {CREATOR, CONTRIBUTOR})
public class PersonMapperUnit extends AgentMapperUnit {

  public PersonMapperUnit(AgentRoleAssigner agentRoleAssigner,
                          ReferenceService referenceService) {
    super(agentRoleAssigner, referenceService);
  }
}
