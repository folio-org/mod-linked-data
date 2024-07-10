package org.folio.linked.data.integration.kafka.sender;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;
import org.folio.linked.data.model.entity.Resource;

public interface DeleteMessageSender extends Function<Resource, Collection<Resource>>, Consumer<Resource> {

  default void produce(Resource resource) {
    apply(resource)
      .forEach(this);
  }
}
