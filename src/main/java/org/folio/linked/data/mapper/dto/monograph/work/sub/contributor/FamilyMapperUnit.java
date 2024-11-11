package org.folio.linked.data.mapper.dto.monograph.work.sub.contributor;

import static org.folio.ld.dictionary.PredicateDictionary.CONTRIBUTOR;
import static org.folio.ld.dictionary.ResourceTypeDictionary.FAMILY;

import org.folio.linked.data.domain.dto.Agent;
import org.folio.linked.data.mapper.dto.common.MapperUnit;
import org.folio.linked.data.mapper.dto.monograph.work.sub.AgentRoleAssigner;
import org.folio.linked.data.service.resource.ResourceMarcAuthorityService;
import org.springframework.stereotype.Component;

@Component("contributorFamilyMapperUnit")
@MapperUnit(type = FAMILY, requestDto = Agent.class, predicate = CONTRIBUTOR)
public class FamilyMapperUnit extends ContributorMapperUnit {

  public FamilyMapperUnit(AgentRoleAssigner agentRoleAssigner,
                          ResourceMarcAuthorityService resourceMarcAuthorityService) {
    super(agentRoleAssigner, resourceMarcAuthorityService);
  }
}
