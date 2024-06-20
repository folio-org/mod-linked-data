package org.folio.linked.data.mapper.kafka;

import java.util.Optional;
import org.folio.linked.data.model.entity.Resource;
import org.folio.search.domain.dto.InstanceIngressPayload;

public interface KafkaInventoryMessageMapper {

  Optional<InstanceIngressPayload> toInstanceIngressPayload(Resource instance);
}
