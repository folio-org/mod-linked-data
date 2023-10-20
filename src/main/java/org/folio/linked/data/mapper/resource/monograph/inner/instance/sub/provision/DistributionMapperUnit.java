package org.folio.linked.data.mapper.resource.monograph.inner.instance.sub.provision;

import static org.folio.ld.dictionary.PredicateDictionary.PE_DISTRIBUTION;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PROVIDER_EVENT;

import org.folio.linked.data.domain.dto.ProviderEvent;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.mapper.resource.monograph.inner.common.PlaceMapperUnit;
import org.springframework.stereotype.Component;

@Component
@MapperUnit(type = PROVIDER_EVENT, predicate = PE_DISTRIBUTION, dtoClass = ProviderEvent.class)
public class DistributionMapperUnit extends ProviderEventMapperUnit {

  public DistributionMapperUnit(CoreMapper coreMapper, PlaceMapperUnit<ProviderEvent> placeMapper) {
    super(coreMapper, placeMapper, (providerEvent, instance) -> instance.addDistributionItem(providerEvent));
  }
}
