package org.folio.linked.data.mapper.kafka.search.identifier;

import java.util.function.BiFunction;
import java.util.function.Function;
import org.folio.search.domain.dto.BibframeInstancesInnerIdentifiersInner;
import org.springframework.stereotype.Component;

@Component
public class BibframeInstancesInnerIdentifiersInnerMapperAbstract extends
  AbstractIndexIdentifierMapper<BibframeInstancesInnerIdentifiersInner,
    BibframeInstancesInnerIdentifiersInner.TypeEnum> {

  @Override
  protected Function<String, BibframeInstancesInnerIdentifiersInner.TypeEnum> getTypeSupplier() {
    return BibframeInstancesInnerIdentifiersInner.TypeEnum::fromValue;
  }

  @Override
  protected Function<String, BibframeInstancesInnerIdentifiersInner> getIndexCreateByValueFunction() {
    return s -> new BibframeInstancesInnerIdentifiersInner().value(s);
  }

  @Override
  protected BiFunction<BibframeInstancesInnerIdentifiersInner, BibframeInstancesInnerIdentifiersInner.TypeEnum,
    BibframeInstancesInnerIdentifiersInner> getIndexUpdateTypeFunction() {
    return BibframeInstancesInnerIdentifiersInner::type;
  }
}
