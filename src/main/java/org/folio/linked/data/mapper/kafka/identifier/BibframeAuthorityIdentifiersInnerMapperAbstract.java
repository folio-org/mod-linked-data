package org.folio.linked.data.mapper.kafka.identifier;

import java.util.function.BiFunction;
import java.util.function.Function;
import org.folio.search.domain.dto.BibframeAuthorityIdentifiersInner;
import org.springframework.stereotype.Component;

@Component
public class BibframeAuthorityIdentifiersInnerMapperAbstract extends
  AbstractIndexIdentifierMapper<BibframeAuthorityIdentifiersInner, BibframeAuthorityIdentifiersInner.TypeEnum> {

  @Override
  protected Function<String, BibframeAuthorityIdentifiersInner.TypeEnum> getTypeSupplier() {
    return BibframeAuthorityIdentifiersInner.TypeEnum::fromValue;
  }

  @Override
  protected Function<String, BibframeAuthorityIdentifiersInner> getIndexCreateByValueFunction() {
    return s -> new BibframeAuthorityIdentifiersInner().value(s);
  }

  @Override
  protected BiFunction<BibframeAuthorityIdentifiersInner, BibframeAuthorityIdentifiersInner.TypeEnum,
    BibframeAuthorityIdentifiersInner> getIndexUpdateTypeFunction() {
    return BibframeAuthorityIdentifiersInner::type;
  }
}
