package org.folio.linked.data.mapper.dto.monograph.work.sub.creator;

import static org.folio.ld.dictionary.PredicateDictionary.CREATOR;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PERSON;

import org.folio.linked.data.domain.dto.PersonField;
import org.folio.linked.data.mapper.dto.common.CoreMapper;
import org.folio.linked.data.mapper.dto.common.MapperUnit;
import org.folio.linked.data.mapper.dto.monograph.work.sub.AgentRoleAssigner;
import org.folio.linked.data.service.HashService;
import org.springframework.stereotype.Component;

@Component("CreatorPersonMapperUnit")
@MapperUnit(type = PERSON, dtoClass = PersonField.class, predicate = CREATOR)
public class PersonMapperUnit extends CreatorMapperUnit {

  public PersonMapperUnit(CoreMapper coreMapper, HashService hashService, AgentRoleAssigner agentRoleAssigner) {
    super(coreMapper, hashService, PERSON_TO_FIELD_CONVERTER, FIELD_TO_PERSON_CONVERTER, agentRoleAssigner, PERSON);
  }

}
