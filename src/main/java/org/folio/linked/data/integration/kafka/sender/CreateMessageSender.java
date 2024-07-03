package org.folio.linked.data.integration.kafka.sender;

import java.util.function.Consumer;
import java.util.function.Predicate;
import org.folio.linked.data.model.entity.Resource;

public interface CreateMessageSender extends Predicate<Resource>, Consumer<Resource> {

  default void produce(Resource resource) {
    if (test(resource)) {
      accept(resource);
    }
  }
}
