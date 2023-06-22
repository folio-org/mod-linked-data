package org.folio.linked.data.mapper.monograph.inner.work.sub;

import org.folio.linked.data.domain.dto.Work;
import org.folio.linked.data.mapper.resource.inner.sub.SubResourceMapper;

public interface WorkSubResourceMapper extends SubResourceMapper<Work> {

  @Override
  default Class<Work> destinationDtoClass() {
    return Work.class;
  }
}
