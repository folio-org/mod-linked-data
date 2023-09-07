package org.folio.linked.data.mapper.resource.monograph.inner.instance.sub;

import static org.folio.linked.data.util.Bibframe2Constants.CARRIER2_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.CARRIER_URL;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Instance2;
import org.folio.linked.data.domain.dto.Property2;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.mapper.resource.common.inner.sub.SubResourceMapper;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = CARRIER_URL, predicate = CARRIER2_PRED, dtoClass = Property2.class)
public class Carrier2MapperUnit implements Instance2SubResourceMapperUnit {

  private final CoreMapper coreMapper;

  @Override
  public Instance2 toDto(Resource source, Instance2 destination) {
    var property = coreMapper.toProperty(source);
    destination.addCarrierItem(property);
    return destination;
  }

  @Override
  public Resource toEntity(Object dto, String predicate, SubResourceMapper subResourceMapper) {
    return coreMapper.propertyToEntity((Property2) dto, CARRIER_URL);
  }
}
