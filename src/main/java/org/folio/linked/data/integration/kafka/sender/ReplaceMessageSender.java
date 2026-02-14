package org.folio.linked.data.integration.kafka.sender;

import static org.folio.ld.dictionary.ResourceTypeDictionary.LIGHT_RESOURCE;

import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.data.util.Pair;

public interface ReplaceMessageSender extends Consumer<Pair<Resource, Resource>>,
  BiFunction<Resource, Resource, Collection<Pair<Resource, Resource>>> {

  default void produce(Resource previous, Resource current) {
    if (current.isOfType(LIGHT_RESOURCE)) {
      return;
    }
    apply(previous, current)
      .forEach(this);
  }

}
