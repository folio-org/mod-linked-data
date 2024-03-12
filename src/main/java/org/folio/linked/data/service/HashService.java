package org.folio.linked.data.service;


import lombok.NonNull;
import org.folio.linked.data.model.entity.Resource;

public interface HashService {

  Long hash(@NonNull Resource resource);

}
