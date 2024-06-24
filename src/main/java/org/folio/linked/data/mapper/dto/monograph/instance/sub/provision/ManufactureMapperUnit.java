package org.folio.linked.data.mapper.dto.monograph.instance.sub.provision;

import static org.folio.ld.dictionary.PredicateDictionary.PE_MANUFACTURE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PROVIDER_EVENT;

import org.folio.linked.data.domain.dto.ProviderEvent;
import org.folio.linked.data.mapper.dto.common.CoreMapper;
import org.folio.linked.data.mapper.dto.common.MapperUnit;
import org.folio.linked.data.service.HashService;
import org.springframework.stereotype.Component;

@Component
@MapperUnit(type = PROVIDER_EVENT, predicate = PE_MANUFACTURE, requestDto = ProviderEvent.class)
public class ManufactureMapperUnit extends ProviderEventMapperUnit {

  public ManufactureMapperUnit(CoreMapper coreMapper, HashService hashService) {
    super(coreMapper, hashService, (providerEvent, instance) -> instance.addManufactureItem(providerEvent));
  }
}
