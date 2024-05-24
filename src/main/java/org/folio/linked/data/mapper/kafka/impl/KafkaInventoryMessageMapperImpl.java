package org.folio.linked.data.mapper.kafka.impl;

import org.folio.linked.data.mapper.kafka.KafkaInventoryMessageMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.search.domain.dto.InstanceIngressEvent;
import org.folio.search.domain.dto.InstanceIngressPayload;

import java.util.Optional;

public class KafkaInventoryMessageMapperImpl implements KafkaInventoryMessageMapper {

  @Override
  public Optional<InstanceIngressPayload> toInstanceIngressPayload(Resource instance) {
    return Optional.empty();
  }

}
