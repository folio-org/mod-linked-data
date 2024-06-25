package org.folio.linked.data.service;

import org.folio.linked.data.model.entity.Resource;

public interface KafkaSender {

  void sendSingleResourceCreated(Resource resource);

  boolean sendMultipleResourceCreated(Resource resource);

  void sendResourceUpdated(Resource newResource, Resource oldResource);

  void sendResourceDeleted(Resource resource);

  void sendAuthorityCreated(Resource resource);

}
