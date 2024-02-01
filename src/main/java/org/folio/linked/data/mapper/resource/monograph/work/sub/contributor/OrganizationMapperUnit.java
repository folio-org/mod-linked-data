package org.folio.linked.data.mapper.resource.monograph.work.sub.contributor;

import static org.folio.ld.dictionary.PredicateDictionary.CONTRIBUTOR;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ORGANIZATION;

import org.folio.linked.data.domain.dto.OrganizationField;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.mapper.resource.monograph.work.sub.AgentRoleAssigner;
import org.springframework.stereotype.Component;

@Component("ContributorOrganizationMapperUnit")
@MapperUnit(type = ORGANIZATION, dtoClass = OrganizationField.class, predicate = CONTRIBUTOR)
public class OrganizationMapperUnit extends ContributorMapperUnit {

  public OrganizationMapperUnit(CoreMapper coreMapper, AgentRoleAssigner agentRoleAssigner) {
    super(coreMapper, ORG_TO_FIELD_CONVERTER, FIELD_TO_ORG_CONVERTER, agentRoleAssigner, ORGANIZATION);
  }

}
