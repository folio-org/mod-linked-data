package org.folio.linked.data.mapper.resource.monograph.inner.instance.sub.provision;

import static org.folio.linked.data.util.BibframeConstants.MANUFACTURE;
import static org.folio.linked.data.util.BibframeConstants.MANUFACTURE_URL;
import static org.folio.linked.data.util.BibframeConstants.PLACE_PRED;
import static org.folio.linked.data.util.BibframeConstants.PROVISION_ACTIVITY_PRED;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.domain.dto.ManufactureField;
import org.folio.linked.data.mapper.resource.common.CommonMapper;
import org.folio.linked.data.mapper.resource.common.ResourceMapper;
import org.folio.linked.data.mapper.resource.monograph.inner.instance.sub.InstanceSubResourceMapper;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ResourceMapper(type = MANUFACTURE, predicate = PROVISION_ACTIVITY_PRED, dtoClass = ManufactureField.class)
public class ManufactureMapper implements InstanceSubResourceMapper {

  private final CommonMapper commonMapper;

  @Override
  public Instance toDto(Resource source, Instance destination) {
    var manufacture = commonMapper.toProvisionActivity(source);
    commonMapper.addMappedProperties(source, PLACE_PRED, manufacture::addPlaceItem);
    return destination.addProvisionActivityItem(new ManufactureField().manufacture(manufacture));
  }

  @Override
  public Resource toEntity(Object dto, String predicate) {
    var manufacture = ((ManufactureField) dto).getManufacture();
    return commonMapper.provisionActivityToEntity(manufacture, MANUFACTURE_URL, MANUFACTURE);
  }

}
