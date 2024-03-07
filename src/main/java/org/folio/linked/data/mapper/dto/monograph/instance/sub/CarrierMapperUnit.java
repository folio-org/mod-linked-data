package org.folio.linked.data.mapper.dto.monograph.instance.sub;

import static org.folio.ld.dictionary.PredicateDictionary.CARRIER;
import static org.folio.ld.dictionary.ResourceTypeDictionary.CATEGORY;

import org.folio.linked.data.domain.dto.Category;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.domain.dto.InstanceReference;
import org.folio.linked.data.mapper.dto.common.CoreMapper;
import org.folio.linked.data.mapper.dto.common.MapperUnit;
import org.folio.linked.data.mapper.dto.monograph.common.CategoryMapperUnit;
import org.folio.linked.data.service.HashService;
import org.springframework.stereotype.Component;

@Component
@MapperUnit(type = CATEGORY, predicate = CARRIER, dtoClass = Category.class)
public class CarrierMapperUnit extends CategoryMapperUnit {

  public CarrierMapperUnit(CoreMapper coreMapper, HashService hashService) {
    super(coreMapper, hashService, (category, destination) -> {
      if (destination instanceof Instance instance) {
        instance.addCarrierItem(category);
      }
      if (destination instanceof InstanceReference instance) {
        instance.addCarrierItem(category);
      }
    }, CATEGORY);
  }
}
