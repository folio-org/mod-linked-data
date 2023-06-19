package org.folio.linked.data.mapper;

import org.folio.linked.data.domain.dto.Work;
import org.folio.linked.data.model.entity.Resource;

public interface WorkMapper {

  Work toWork(Resource resource);

}
