package org.folio.linked.data.mapper.resource.monograph.inner.work.sub.creator;

import static org.folio.linked.data.util.BibframeConstants.CREATOR_PRED;
import static org.folio.linked.data.util.BibframeConstants.PERSON;

import org.folio.linked.data.domain.dto.Agent;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.springframework.stereotype.Component;

@Component("CreatorPersonMapperUnit")
@MapperUnit(type = PERSON, dtoClass = Agent.class, predicate = CREATOR_PRED)
public class PersonMapperUnit extends CreatorMapperUnit {
  public PersonMapperUnit(CoreMapper coreMapper) {
    super(coreMapper, PERSON_CONVERTER);
  }
}
