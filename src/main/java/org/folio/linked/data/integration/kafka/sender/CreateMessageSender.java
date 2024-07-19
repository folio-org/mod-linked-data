package org.folio.linked.data.integration.kafka.sender;

import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Function;
import org.folio.linked.data.model.entity.Resource;

public interface CreateMessageSender
  extends Function<Resource, Collection<Resource>>, BiConsumer<Resource, Boolean> {

  default void produce(Resource resource, Boolean putIndexDate) {
    apply(resource)
      .forEach(r -> accept(r, putIndexDate));
  }
}
