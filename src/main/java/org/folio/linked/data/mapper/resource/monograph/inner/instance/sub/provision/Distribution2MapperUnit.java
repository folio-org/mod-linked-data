package org.folio.linked.data.mapper.resource.monograph.inner.instance.sub.provision;

import static org.folio.linked.data.util.BibframeConstants.DISTRIBUTION;
import static org.folio.linked.data.util.BibframeConstants.DISTRIBUTION_URL;
import static org.folio.linked.data.util.BibframeConstants.PLACE_PRED;
import static org.folio.linked.data.util.BibframeConstants.PROVISION_ACTIVITY_PRED;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.DistributionField2;
import org.folio.linked.data.domain.dto.Instance2;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.mapper.resource.common.inner.sub.SubResourceMapper;
import org.folio.linked.data.mapper.resource.monograph.inner.instance.sub.Instance2SubResourceMapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = DISTRIBUTION, predicate = PROVISION_ACTIVITY_PRED, dtoClass = DistributionField2.class)
public class Distribution2MapperUnit implements Instance2SubResourceMapperUnit {

  private final CoreMapper coreMapper;

  @Override
  public Instance2 toDto(Resource source, Instance2 destination) {
    var distribution = coreMapper.toProvisionActivity(source);
    coreMapper.addMappedProperties(source, PLACE_PRED, distribution::addPlaceItem);
    return destination.addProvisionActivityItem(new DistributionField2().distribution(distribution));
  }

  @Override
  public Resource toEntity(Object dto, String predicate, SubResourceMapper subResourceMapper) {
    var distribution = ((DistributionField2) dto).getDistribution();
    return coreMapper.provisionActivityToEntity(distribution, DISTRIBUTION_URL, DISTRIBUTION);
  }

}
