package org.folio.linked.data.mapper.kafka;

import org.folio.linked.data.model.entity.Resource;
import org.folio.search.domain.dto.InstanceIngressEvent;
import org.folio.search.domain.dto.InstanceIngressPayload;

import java.util.Optional;

public interface KafkaInventoryMessageMapper {

  Optional<InstanceIngressPayload> toInstanceIngressPayload(Resource instance);
}
