package org.folio.linked.data.mapper.dto.monograph.work.sub.contributor;

import static org.folio.ld.dictionary.PredicateDictionary.CONTRIBUTOR;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PERSON;

import org.folio.linked.data.domain.dto.PersonField;
import org.folio.linked.data.mapper.dto.common.CoreMapper;
import org.folio.linked.data.mapper.dto.common.MapperUnit;
import org.folio.linked.data.mapper.dto.monograph.work.sub.AgentMapperUnit;
import org.folio.linked.data.mapper.dto.monograph.work.sub.AgentRoleAssigner;
import org.folio.linked.data.service.HashService;
import org.springframework.stereotype.Component;

@Component("ContributorPersonMapperUnit")
@MapperUnit(type = PERSON, dtoClass = PersonField.class, predicate = CONTRIBUTOR)
public class PersonMapperUnit extends ContributorMapperUnit {

  public PersonMapperUnit(CoreMapper coreMapper, HashService hashService, AgentRoleAssigner agentRoleAssigner) {
    super(coreMapper, hashService, AgentMapperUnit.PERSON_TO_FIELD_CONVERTER, FIELD_TO_PERSON_CONVERTER,
      agentRoleAssigner, PERSON);
  }

}
