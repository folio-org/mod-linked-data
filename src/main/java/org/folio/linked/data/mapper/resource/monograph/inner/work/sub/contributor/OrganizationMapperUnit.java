package org.folio.linked.data.mapper.resource.monograph.inner.work.sub.contributor;

import static org.folio.ld.dictionary.PredicateDictionary.CONTRIBUTOR;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ORGANIZATION;

import org.folio.linked.data.domain.dto.Agent;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.springframework.stereotype.Component;

@Component("ContributorOrganizationMapperUnit")
@MapperUnit(type = ORGANIZATION, dtoClass = Agent.class, predicate = CONTRIBUTOR)
public class OrganizationMapperUnit extends ContributorMapperUnit {
  public OrganizationMapperUnit(CoreMapper coreMapper) {
    super(coreMapper, ORG_CONVERTER);
  }
}
