package org.folio.linked.data.mapper.resource.monograph.work.sub.contributor;

import static org.folio.ld.dictionary.PredicateDictionary.CONTRIBUTOR;
import static org.folio.ld.dictionary.ResourceTypeDictionary.FAMILY;

import org.folio.linked.data.domain.dto.FamilyField;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.mapper.resource.monograph.work.sub.AgentMapperUnit;
import org.springframework.stereotype.Component;

@Component("ContributorFamilyMapperUnit")
@MapperUnit(type = FAMILY, dtoClass = FamilyField.class, predicate = CONTRIBUTOR)
public class FamilyMapperUnit extends ContributorMapperUnit {
  public FamilyMapperUnit(CoreMapper coreMapper) {
    super(coreMapper, AgentMapperUnit.FAMILY_TO_FIELD_CONVERTER, AgentMapperUnit.FIELD_TO_FAMILY_CONVERTER, FAMILY);
  }
}
