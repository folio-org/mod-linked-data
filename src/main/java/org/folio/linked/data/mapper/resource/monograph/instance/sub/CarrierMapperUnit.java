package org.folio.linked.data.mapper.resource.monograph.instance.sub;

import static org.folio.ld.dictionary.PredicateDictionary.CARRIER;
import static org.folio.ld.dictionary.ResourceTypeDictionary.CATEGORY;

import org.folio.linked.data.domain.dto.Category;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.springframework.stereotype.Component;

@Component
@MapperUnit(type = CATEGORY, predicate = CARRIER, dtoClass = Category.class)
public class CarrierMapperUnit extends CategoryMapperUnit {


  public CarrierMapperUnit(CoreMapper coreMapper) {
    super(coreMapper, (category, instance) -> instance.addCarrierItem(category), CATEGORY);
  }
}
