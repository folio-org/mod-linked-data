package org.folio.linked.data.mapper.resource.monograph.instance.sub.provision;

import static org.folio.ld.dictionary.PredicateDictionary.PE_PRODUCTION;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PROVIDER_EVENT;

import org.folio.linked.data.domain.dto.ProviderEvent;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.mapper.resource.monograph.common.PlaceMapperUnit;
import org.springframework.stereotype.Component;

@Component
@MapperUnit(type = PROVIDER_EVENT, predicate = PE_PRODUCTION, dtoClass = ProviderEvent.class)
public class ProductionMapperUnit extends ProviderEventMapperUnit {

  public ProductionMapperUnit(CoreMapper coreMapper, PlaceMapperUnit placeMapper) {
    super(coreMapper, placeMapper, (providerEvent, instance) -> instance.addProductionItem(providerEvent));
  }
}
