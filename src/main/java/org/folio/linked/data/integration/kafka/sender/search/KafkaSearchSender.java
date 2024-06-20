package org.folio.linked.data.integration.kafka.sender.search;

import org.folio.linked.data.model.entity.Resource;

public interface KafkaSearchSender {

  void sendSingleResourceCreated(Resource resource);

  boolean sendMultipleResourceCreated(Resource resource);

  void sendResourceUpdated(Resource newResource, Resource oldResource);

  void sendResourceDeleted(Resource resource);

}
