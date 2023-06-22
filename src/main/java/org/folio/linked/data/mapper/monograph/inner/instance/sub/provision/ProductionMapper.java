package org.folio.linked.data.mapper.monograph.inner.instance.sub.provision;

import static org.folio.linked.data.util.BibframeConstants.PLACE_PRED;
import static org.folio.linked.data.util.BibframeConstants.PRODUCTION;
import static org.folio.linked.data.util.MappingUtil.addMappedProperties;
import static org.folio.linked.data.util.MappingUtil.toProvisionActivity;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.domain.dto.ProductionField;
import org.folio.linked.data.mapper.monograph.inner.instance.sub.InstanceSubResourceMapper;
import org.folio.linked.data.mapper.resource.ResourceMapper;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ResourceMapper(PRODUCTION)
public class ProductionMapper implements InstanceSubResourceMapper {

  private final ObjectMapper objectMapper;

  @Override
  public Instance toDto(Resource source, Instance destination) {
    var production = toProvisionActivity(objectMapper, source);
    addMappedProperties(objectMapper, source, PLACE_PRED, production::addPlaceItem);
    return destination.addProvisionActivityItem(new ProductionField().production(production));
  }

}
