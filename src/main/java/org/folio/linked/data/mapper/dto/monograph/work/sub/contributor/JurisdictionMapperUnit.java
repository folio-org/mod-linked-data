package org.folio.linked.data.mapper.dto.monograph.work.sub.contributor;

import static org.folio.ld.dictionary.PredicateDictionary.CONTRIBUTOR;
import static org.folio.ld.dictionary.ResourceTypeDictionary.JURISDICTION;

import org.folio.linked.data.domain.dto.Agent;
import org.folio.linked.data.mapper.dto.common.MapperUnit;
import org.folio.linked.data.mapper.dto.monograph.work.sub.AgentRoleAssigner;
import org.folio.linked.data.service.resource.marc.ResourceMarcAuthorityService;
import org.springframework.stereotype.Component;

@Component("contributorJurisdictionMapperUnit")
@MapperUnit(type = JURISDICTION, requestDto = Agent.class, predicate = CONTRIBUTOR)
public class JurisdictionMapperUnit extends ContributorMapperUnit {

  public JurisdictionMapperUnit(AgentRoleAssigner agentRoleAssigner,
                                ResourceMarcAuthorityService resourceMarcAuthorityService) {
    super(agentRoleAssigner, resourceMarcAuthorityService);
  }
}
