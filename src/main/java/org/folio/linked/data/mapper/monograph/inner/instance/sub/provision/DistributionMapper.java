package org.folio.linked.data.mapper.monograph.inner.instance.sub.provision;

import static org.folio.linked.data.util.BibframeConstants.DISTRIBUTION;
import static org.folio.linked.data.util.BibframeConstants.PLACE_PRED;
import static org.folio.linked.data.util.MappingUtil.addMappedProperties;
import static org.folio.linked.data.util.MappingUtil.toProvisionActivity;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.DistributionField;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.mapper.monograph.inner.instance.sub.InstanceSubResourceMapper;
import org.folio.linked.data.mapper.resource.ResourceMapper;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ResourceMapper(DISTRIBUTION)
public class DistributionMapper implements InstanceSubResourceMapper {

  private final ObjectMapper objectMapper;

  @Override
  public Instance toDto(Resource source, Instance destination) {
    var distribution = toProvisionActivity(objectMapper, source);
    addMappedProperties(objectMapper, source, PLACE_PRED, distribution::addPlaceItem);
    return destination.addProvisionActivityItem(new DistributionField().distribution(distribution));
  }

}
