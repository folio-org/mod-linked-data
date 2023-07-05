package org.folio.linked.data.mapper.resource.monograph.inner.work.sub;

import org.folio.linked.data.domain.dto.Work;
import org.folio.linked.data.mapper.resource.common.inner.sub.SubResourceMapperUnit;
import org.folio.linked.data.model.entity.Resource;

public interface WorkSubResourceMapperUnit extends SubResourceMapperUnit<Work> {

  @Override
  default Class<Work> getParentDto() {
    return Work.class;
  }

  // required until we implement it for every Work mapper. Should be removed after.
  default Resource toEntity(Object dto, String predicate) {
    return null;
  }

}
