package org.folio.linked.data.mapper.dto.monograph.work.sub.creator;

import static org.folio.ld.dictionary.PredicateDictionary.CREATOR;
import static org.folio.ld.dictionary.ResourceTypeDictionary.FAMILY;

import org.folio.linked.data.domain.dto.FamilyField;
import org.folio.linked.data.mapper.dto.common.CoreMapper;
import org.folio.linked.data.mapper.dto.common.MapperUnit;
import org.folio.linked.data.mapper.dto.monograph.work.sub.AgentMapperUnit;
import org.folio.linked.data.mapper.dto.monograph.work.sub.AgentRoleAssigner;
import org.folio.linked.data.service.HashService;
import org.springframework.stereotype.Component;

@Component("CreatorFamilyMapperUnit")
@MapperUnit(type = FAMILY, dtoClass = FamilyField.class, predicate = CREATOR)
public class FamilyMapperUnit extends CreatorMapperUnit {

  public FamilyMapperUnit(CoreMapper coreMapper, HashService hashService, AgentRoleAssigner agentRoleAssigner) {
    super(coreMapper, hashService, AgentMapperUnit.FAMILY_TO_FIELD_CONVERTER, AgentMapperUnit.FIELD_TO_FAMILY_CONVERTER,
      agentRoleAssigner, FAMILY);
  }

}
