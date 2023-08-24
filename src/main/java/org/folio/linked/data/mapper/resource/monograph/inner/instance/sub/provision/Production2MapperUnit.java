package org.folio.linked.data.mapper.resource.monograph.inner.instance.sub.provision;

import static org.folio.linked.data.util.BibframeConstants.PLACE_PRED;
import static org.folio.linked.data.util.BibframeConstants.PRODUCTION;
import static org.folio.linked.data.util.BibframeConstants.PRODUCTION_URL;
import static org.folio.linked.data.util.BibframeConstants.PROVISION_ACTIVITY_PRED;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Instance2;
import org.folio.linked.data.domain.dto.ProductionField2;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.mapper.resource.common.inner.sub.SubResourceMapper;
import org.folio.linked.data.mapper.resource.monograph.inner.instance.sub.Instance2SubResourceMapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = PRODUCTION, predicate = PROVISION_ACTIVITY_PRED, dtoClass = ProductionField2.class)
public class Production2MapperUnit implements Instance2SubResourceMapperUnit {

  private final CoreMapper coreMapper;

  @Override
  public Instance2 toDto(Resource source, Instance2 destination) {
    var production = coreMapper.toProvisionActivity(source);
    coreMapper.addMappedProperties(source, PLACE_PRED, production::addPlaceItem);
    return destination.addProvisionActivityItem(new ProductionField2().production(production));
  }

  @Override
  public Resource toEntity(Object dto, String predicate, SubResourceMapper subResourceMapper) {
    var production = ((ProductionField2) dto).getProduction();
    return coreMapper.provisionActivityToEntity(production, PRODUCTION_URL, PRODUCTION);
  }
}
