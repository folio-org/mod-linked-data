package org.folio.linked.data.mapper.resource.monograph.inner.instance.sub.provision;

import static org.folio.linked.data.util.BibframeConstants.DISTRIBUTION;
import static org.folio.linked.data.util.BibframeConstants.DISTRIBUTION_URL;
import static org.folio.linked.data.util.BibframeConstants.PLACE_PRED;
import static org.folio.linked.data.util.BibframeConstants.PROVISION_ACTIVITY_PRED;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.DistributionField;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.mapper.resource.common.inner.sub.SubResourceMapper;
import org.folio.linked.data.mapper.resource.monograph.inner.instance.sub.InstanceSubResourceMapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = DISTRIBUTION, predicate = PROVISION_ACTIVITY_PRED, dtoClass = DistributionField.class)
public class DistributionMapperUnit implements InstanceSubResourceMapperUnit {

  private final CoreMapper coreMapper;

  @Override
  public Instance toDto(Resource source, Instance destination) {
    var distribution = coreMapper.toProvisionActivity(source);
    coreMapper.addMappedProperties(source, PLACE_PRED, distribution::addPlaceItem);
    return destination.addProvisionActivityItem(new DistributionField().distribution(distribution));
  }

  @Override
  public Resource toEntity(Object dto, String predicate, SubResourceMapper subResourceMapper) {
    var distribution = ((DistributionField) dto).getDistribution();
    return coreMapper.provisionActivityToEntity(distribution, DISTRIBUTION_URL, DISTRIBUTION);
  }

}
