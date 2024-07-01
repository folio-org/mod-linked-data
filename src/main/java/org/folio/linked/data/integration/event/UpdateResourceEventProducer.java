package org.folio.linked.data.integration.event;

import java.util.function.BiConsumer;
import java.util.function.Predicate;
import org.folio.linked.data.model.entity.Resource;

public interface UpdateResourceEventProducer extends Predicate<Resource>, BiConsumer<Resource, Resource> {

  default void produce(Resource resource, Resource newResource) {
    if (test(resource)) {
      accept(resource, newResource);
    }
  }
}
