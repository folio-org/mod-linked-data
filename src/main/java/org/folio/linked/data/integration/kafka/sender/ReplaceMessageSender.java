package org.folio.linked.data.integration.kafka.sender;

import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.data.util.Pair;

public interface ReplaceMessageSender extends Consumer<Pair<Resource, Resource>>,
  BiFunction<Resource, Resource, Collection<Pair<Resource, Resource>>> {

  default void produce(Resource previous, Resource current) {
    apply(previous, current)
      .forEach(this);
  }

}
