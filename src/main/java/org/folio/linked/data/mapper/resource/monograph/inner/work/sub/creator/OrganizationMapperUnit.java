package org.folio.linked.data.mapper.resource.monograph.inner.work.sub.creator;

import static org.folio.ld.dictionary.PredicateDictionary.CREATOR;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ORGANIZATION;

import org.folio.linked.data.domain.dto.Agent;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.springframework.stereotype.Component;

@Component("CreatorOrganizationMapperUnit")
@MapperUnit(type = ORGANIZATION, dtoClass = Agent.class, predicate = CREATOR)
public class OrganizationMapperUnit extends CreatorMapperUnit {
  public OrganizationMapperUnit(CoreMapper coreMapper) {
    super(coreMapper, ORG_CONVERTER);
  }
}
