package org.folio.linked.data.integration.kafka.sender;

import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import org.folio.linked.data.model.entity.Resource;

public interface UpdateMessageSender extends BiConsumer<Resource, Resource>,
  BiFunction<Resource, Resource, Collection<UpdateMessageSender.ResourcePair>> {

  default void produce(Resource oldResource, Resource newResource) {
    apply(oldResource, newResource)
      .forEach(pair -> this.accept(pair.oldResource, pair.newResource));
  }

  record ResourcePair(Resource oldResource, Resource newResource) {
  }
}
