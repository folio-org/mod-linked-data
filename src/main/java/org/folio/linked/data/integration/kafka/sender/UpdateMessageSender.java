package org.folio.linked.data.integration.kafka.sender;

import java.util.function.BiConsumer;
import java.util.function.Predicate;
import org.folio.linked.data.model.entity.Resource;

public interface UpdateMessageSender extends Predicate<Resource>, BiConsumer<Resource, Resource> {

  default void produce(Resource oldResource, Resource newResource) {
    if (test(oldResource) && test(newResource)) {
      accept(oldResource, newResource);
    }
  }
}
