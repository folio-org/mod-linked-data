package org.folio.linked.data.integration.kafka.sender.inventory;

import org.folio.linked.data.model.entity.Resource;

public interface KafkaInventorySender {

  void sendInstanceCreated(Resource resource);

}
