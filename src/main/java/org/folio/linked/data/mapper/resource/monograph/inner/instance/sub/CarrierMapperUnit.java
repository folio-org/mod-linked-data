package org.folio.linked.data.mapper.resource.monograph.inner.instance.sub;

import static org.folio.linked.data.util.BibframeConstants.CARRIER_PRED;
import static org.folio.linked.data.util.BibframeConstants.CATEGORY;

import org.folio.linked.data.domain.dto.Category;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.mapper.resource.monograph.inner.common.CategoryMapperUnit;
import org.folio.linked.data.service.dictionary.ResourceTypeService;
import org.springframework.stereotype.Component;

@Component
@MapperUnit(type = CATEGORY, predicate = CARRIER_PRED, dtoClass = Category.class)
public class CarrierMapperUnit extends CategoryMapperUnit {


  public CarrierMapperUnit(CoreMapper coreMapper, ResourceTypeService resourceTypeService) {
    super(coreMapper, resourceTypeService, (category, instance) -> instance.addCarrierItem(category), CATEGORY);
  }
}
