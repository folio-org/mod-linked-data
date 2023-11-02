package org.folio.linked.data.mapper.resource.monograph.inner.work.sub.creator;

import static org.folio.ld.dictionary.PredicateDictionary.CREATOR;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ORGANIZATION;

import org.folio.linked.data.domain.dto.OrganizationField;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.springframework.stereotype.Component;

@Component("CreatorOrganizationMapperUnit")
@MapperUnit(type = ORGANIZATION, dtoClass = OrganizationField.class, predicate = CREATOR)
public class OrganizationMapperUnit extends CreatorMapperUnit {
  public OrganizationMapperUnit(CoreMapper coreMapper) {
    super(coreMapper, ORG_TO_FIELD_CONVERTER, FIELD_TO_ORG_CONVERTER, ORGANIZATION);
  }
}
