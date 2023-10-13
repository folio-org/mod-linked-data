package org.folio.linked.data.mapper.resource.monograph.inner.work.sub.contributor;

import static org.folio.linked.data.util.BibframeConstants.CONTRIBUTOR_PRED;
import static org.folio.linked.data.util.BibframeConstants.ORGANIZATION;

import org.folio.linked.data.domain.dto.Agent;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.springframework.stereotype.Component;

@Component("ContributorOrganizationMapperUnit")
@MapperUnit(type = ORGANIZATION, dtoClass = Agent.class, predicate = CONTRIBUTOR_PRED)
public class OrganizationMapperUnit extends ContributorMapperUnit {
  public OrganizationMapperUnit(CoreMapper coreMapper) {
    super(coreMapper, ORG_CONVERTER);
  }
}
