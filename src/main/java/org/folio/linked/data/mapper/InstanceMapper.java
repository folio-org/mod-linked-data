package org.folio.linked.data.mapper;

import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.model.entity.Resource;

public interface InstanceMapper {

  Instance toInstance(Resource resource);

}
