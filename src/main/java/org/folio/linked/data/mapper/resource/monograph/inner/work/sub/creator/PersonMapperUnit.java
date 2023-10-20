package org.folio.linked.data.mapper.resource.monograph.inner.work.sub.creator;

import static org.folio.ld.dictionary.PredicateDictionary.CREATOR;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PERSON;

import org.folio.linked.data.domain.dto.Agent;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.springframework.stereotype.Component;

@Component("CreatorPersonMapperUnit")
@MapperUnit(type = PERSON, dtoClass = Agent.class, predicate = CREATOR)
public class PersonMapperUnit extends CreatorMapperUnit {
  public PersonMapperUnit(CoreMapper coreMapper) {
    super(coreMapper, PERSON_CONVERTER);
  }
}
