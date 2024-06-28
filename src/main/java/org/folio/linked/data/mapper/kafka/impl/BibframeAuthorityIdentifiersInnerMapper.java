package org.folio.linked.data.mapper.kafka.impl;

import org.folio.linked.data.domain.dto.InstanceResponse;
import org.folio.search.domain.dto.BibframeAuthorityIdentifiersInner;
import org.springframework.stereotype.Component;

@Component
public class BibframeAuthorityIdentifiersInnerMapper extends
  IndexIdentifierMapperImpl<BibframeAuthorityIdentifiersInner, BibframeAuthorityIdentifiersInner.TypeEnum> {

  public BibframeAuthorityIdentifiersInnerMapper() {
    super(InstanceResponse.class,
      BibframeAuthorityIdentifiersInner.TypeEnum.class,
      BibframeAuthorityIdentifiersInner.TypeEnum::fromValue,
      s -> new BibframeAuthorityIdentifiersInner().value(s),
      BibframeAuthorityIdentifiersInner::type
    );
  }
}
