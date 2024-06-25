package org.folio.linked.data.mapper.kafka;

import java.util.Optional;
import lombok.NonNull;
import org.folio.linked.data.model.entity.Resource;
import org.folio.search.domain.dto.ResourceEventType;

public interface KafkaMessageMapper<T> {

  Optional<T> toIndex(Resource resource, ResourceEventType eventType);

  Optional<Long> toDeleteIndexId(@NonNull Resource resource);

}
