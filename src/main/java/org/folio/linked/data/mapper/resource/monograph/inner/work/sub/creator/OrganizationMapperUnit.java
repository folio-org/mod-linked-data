package org.folio.linked.data.mapper.resource.monograph.inner.work.sub.creator;

import static org.folio.linked.data.util.BibframeConstants.CREATOR_PRED;
import static org.folio.linked.data.util.BibframeConstants.ORGANIZATION;

import org.folio.linked.data.domain.dto.Agent;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.springframework.stereotype.Component;

@Component("CreatorOrganizationMapperUnit")
@MapperUnit(type = ORGANIZATION, dtoClass = Agent.class, predicate = CREATOR_PRED)
public class OrganizationMapperUnit extends CreatorMapperUnit {
  public OrganizationMapperUnit(CoreMapper coreMapper) {
    super(coreMapper, ORG_CONVERTER);
  }
}
