package org.folio.linked.data.mapper.kafka.inventory;

import java.util.Optional;
import org.folio.linked.data.model.entity.Resource;
import org.folio.search.domain.dto.InstanceIngressEvent;

public interface KafkaInventoryMessageMapper {

  Optional<InstanceIngressEvent> toInstanceIngressEvent(Resource instance);
}
