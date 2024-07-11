package org.folio.linked.data.mapper.kafka.search;

import java.util.Optional;
import org.folio.linked.data.model.entity.Resource;
import org.folio.search.domain.dto.ResourceIndexEventType;

public interface KafkaSearchMessageMapper<T> {

  Optional<T> toIndex(Resource resource, ResourceIndexEventType eventType);

}
