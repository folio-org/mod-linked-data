package org.folio.linked.data.mapper.resource.monograph.inner.work.sub.contributor;

import static org.folio.ld.dictionary.PredicateDictionary.CONTRIBUTOR;
import static org.folio.ld.dictionary.ResourceTypeDictionary.FAMILY;

import org.folio.linked.data.domain.dto.Agent;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.springframework.stereotype.Component;

@Component("ContributorFamilyMapperUnit")
@MapperUnit(type = FAMILY, dtoClass = Agent.class, predicate = CONTRIBUTOR)
public class FamilyMapperUnit extends ContributorMapperUnit {
  public FamilyMapperUnit(CoreMapper coreMapper) {
    super(coreMapper, FAMILY_CONVERTER);
  }
}
