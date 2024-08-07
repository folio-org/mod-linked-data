package org.folio.linked.data.mapper.kafka.search.identifier;

import java.util.function.BiFunction;
import java.util.function.Function;
import org.folio.search.domain.dto.LinkedDataAuthorityIdentifiersInner;
import org.springframework.stereotype.Component;

@Component
public class LinkedDataAuthorityIdentifiersInnerMapperAbstract extends
  AbstractIndexIdentifierMapper<LinkedDataAuthorityIdentifiersInner, LinkedDataAuthorityIdentifiersInner.TypeEnum> {

  @Override
  protected Function<String, LinkedDataAuthorityIdentifiersInner.TypeEnum> getTypeSupplier() {
    return LinkedDataAuthorityIdentifiersInner.TypeEnum::fromValue;
  }

  @Override
  protected Function<String, LinkedDataAuthorityIdentifiersInner> getIndexCreateByValueFunction() {
    return s -> new LinkedDataAuthorityIdentifiersInner().value(s);
  }

  @Override
  protected BiFunction<LinkedDataAuthorityIdentifiersInner, LinkedDataAuthorityIdentifiersInner.TypeEnum,
    LinkedDataAuthorityIdentifiersInner> getIndexUpdateTypeFunction() {
    return LinkedDataAuthorityIdentifiersInner::type;
  }
}
