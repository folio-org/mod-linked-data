package org.folio.linked.data.integration.kafka.sender.search;

import org.folio.linked.data.model.entity.Resource;

public interface KafkaSearchSender {

  void sendWorkCreated(Resource resource);

  boolean sendMultipleWorksCreated(Resource resource);

  void sendWorkUpdated(Resource newResource, Resource oldResource);

  void sendWorkDeleted(Resource resource);

  void sendAuthorityCreated(Resource resource);

}
