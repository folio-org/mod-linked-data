package org.folio.linked.data.mapper.dto.monograph.instance.sub.provision;

import static org.folio.ld.dictionary.PredicateDictionary.PE_PRODUCTION;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PROVIDER_EVENT;

import org.folio.linked.data.domain.dto.ProviderEvent;
import org.folio.linked.data.mapper.dto.common.CoreMapper;
import org.folio.linked.data.mapper.dto.common.MapperUnit;
import org.folio.linked.data.service.HashService;
import org.springframework.stereotype.Component;

@Component
@MapperUnit(type = PROVIDER_EVENT, predicate = PE_PRODUCTION, requestDto = ProviderEvent.class)
public class ProductionMapperUnit extends ProviderEventMapperUnit {

  public ProductionMapperUnit(CoreMapper coreMapper, HashService hashService) {
    super(coreMapper, hashService, (providerEvent, instance) -> instance.addProductionItem(providerEvent));
  }
}
