package org.folio.linked.data.mapper.resource.monograph.inner.work.sub;

import org.folio.linked.data.domain.dto.Work;
import org.folio.linked.data.mapper.resource.common.inner.sub.SubResourceMapperUnit;

public interface WorkSubResourceMapperUnit extends SubResourceMapperUnit<Work> {

  @Override
  default Class<Work> getParentDto() {
    return Work.class;
  }
}
