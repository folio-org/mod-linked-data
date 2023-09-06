package org.folio.linked.data.mapper.resource.monograph.inner.instance.sub.provision;

import static org.folio.linked.data.util.BibframeConstants.DISTRIBUTION_PRED;
import static org.folio.linked.data.util.BibframeConstants.PROVIDER_EVENT;

import org.folio.linked.data.domain.dto.ProviderEvent;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.mapper.resource.monograph.inner.common.PlaceMapperUnit;
import org.folio.linked.data.service.dictionary.ResourceTypeService;
import org.springframework.stereotype.Component;

@Component
@MapperUnit(type = PROVIDER_EVENT, predicate = DISTRIBUTION_PRED, dtoClass = ProviderEvent.class)
public class DistributionMapperUnit extends ProviderEventMapperUnit {

  public DistributionMapperUnit(CoreMapper coreMapper, PlaceMapperUnit<ProviderEvent> placeMapper,
                                ResourceTypeService resourceTypeService) {
    super(coreMapper, placeMapper, resourceTypeService,
      (providerEvent, instance) -> instance.addDistributionItem(providerEvent));
  }
}
