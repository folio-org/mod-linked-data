package org.folio.linked.data.mapper.kafka.search.identifier;

import static org.folio.search.domain.dto.LinkedDataWorkInstancesInnerIdentifiersInner.TypeEnum;

import java.util.function.BiFunction;
import java.util.function.Function;
import org.folio.search.domain.dto.LinkedDataWorkInstancesInnerIdentifiersInner;
import org.springframework.stereotype.Component;

@Component
public class LinkedDataWorkInstancesInnerIdentifiersInnerMapperAbstract extends
  AbstractIndexIdentifierMapper<LinkedDataWorkInstancesInnerIdentifiersInner,
    TypeEnum> {

  @Override
  protected Function<String, TypeEnum> getTypeSupplier() {
    return TypeEnum::fromValue;
  }

  @Override
  protected Function<String, LinkedDataWorkInstancesInnerIdentifiersInner> getIndexCreateByValueFunction() {
    return s -> new LinkedDataWorkInstancesInnerIdentifiersInner().value(s);
  }

  @Override
  protected BiFunction<LinkedDataWorkInstancesInnerIdentifiersInner, TypeEnum,
    LinkedDataWorkInstancesInnerIdentifiersInner> getIndexUpdateTypeFunction() {
    return LinkedDataWorkInstancesInnerIdentifiersInner::type;
  }
}
