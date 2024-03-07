package org.folio.linked.data.service;


import org.folio.linked.data.model.entity.Resource;

public interface HashService {

  Long hash(Resource resource);

}
