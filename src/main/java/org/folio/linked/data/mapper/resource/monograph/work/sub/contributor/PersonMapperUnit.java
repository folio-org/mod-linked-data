package org.folio.linked.data.mapper.resource.monograph.work.sub.contributor;

import static org.folio.ld.dictionary.PredicateDictionary.CONTRIBUTOR;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PERSON;

import org.folio.linked.data.domain.dto.PersonField;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.mapper.resource.monograph.work.sub.AgentMapperUnit;
import org.springframework.stereotype.Component;

@Component("ContributorPersonMapperUnit")
@MapperUnit(type = PERSON, dtoClass = PersonField.class, predicate = CONTRIBUTOR)
public class PersonMapperUnit extends ContributorMapperUnit {
  public PersonMapperUnit(CoreMapper coreMapper) {
    super(coreMapper, AgentMapperUnit.PERSON_TO_FIELD_CONVERTER, AgentMapperUnit.FIELD_TO_PERSON_CONVERTER, PERSON);
  }
}
