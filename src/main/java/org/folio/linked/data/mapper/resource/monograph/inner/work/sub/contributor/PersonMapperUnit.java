package org.folio.linked.data.mapper.resource.monograph.inner.work.sub.contributor;

import static org.folio.linked.data.util.BibframeConstants.CONTRIBUTOR_PRED;
import static org.folio.linked.data.util.BibframeConstants.PERSON;

import org.folio.linked.data.domain.dto.Agent;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.springframework.stereotype.Component;

@Component("ContributorPersonMapperUnit")
@MapperUnit(type = PERSON, dtoClass = Agent.class, predicate = CONTRIBUTOR_PRED)
public class PersonMapperUnit extends ContributorMapperUnit {
  public PersonMapperUnit(CoreMapper coreMapper) {
    super(coreMapper, PERSON_CONVERTER);
  }
}
