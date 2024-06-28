package org.folio.linked.data.mapper.kafka.impl;

import org.folio.linked.data.domain.dto.InstanceResponse;
import org.folio.search.domain.dto.BibframeInstancesInnerIdentifiersInner;
import org.springframework.stereotype.Component;

@Component
public class BibframeInstancesInnerIdentifiersInnerMapper extends
  IndexIdentifierMapperImpl<BibframeInstancesInnerIdentifiersInner, BibframeInstancesInnerIdentifiersInner.TypeEnum> {

  public BibframeInstancesInnerIdentifiersInnerMapper() {
    super(InstanceResponse.class,
      BibframeInstancesInnerIdentifiersInner.TypeEnum.class,
      BibframeInstancesInnerIdentifiersInner.TypeEnum::fromValue,
      s -> new BibframeInstancesInnerIdentifiersInner().value(s),
      BibframeInstancesInnerIdentifiersInner::type
    );
  }
}
