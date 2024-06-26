package org.folio.linked.data.integration.event;

import java.util.function.Consumer;
import java.util.function.Predicate;
import org.folio.linked.data.model.entity.Resource;

public interface CreateResourceEventProducer extends Predicate<Resource>, Consumer<Resource> {

  default void produce(Resource resource) {
    if (test(resource)) {
      accept(resource);
    }
  }
}
