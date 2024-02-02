package org.folio.linked.data.mapper.resource.monograph.instance.sub.provision;

import static org.folio.ld.dictionary.PredicateDictionary.PE_MANUFACTURE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PROVIDER_EVENT;

import org.folio.linked.data.domain.dto.ProviderEvent;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.springframework.stereotype.Component;

@Component
@MapperUnit(type = PROVIDER_EVENT, predicate = PE_MANUFACTURE, dtoClass = ProviderEvent.class)
public class ManufactureMapperUnit extends ProviderEventMapperUnit {

  public ManufactureMapperUnit(CoreMapper coreMapper) {
    super(coreMapper, (providerEvent, instance) -> instance.addManufactureItem(providerEvent));
  }
}
