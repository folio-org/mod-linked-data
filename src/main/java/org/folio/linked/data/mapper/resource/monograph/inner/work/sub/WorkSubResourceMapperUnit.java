package org.folio.linked.data.mapper.resource.monograph.inner.work.sub;

import java.util.Set;
import org.folio.linked.data.domain.dto.Work;
import org.folio.linked.data.mapper.resource.common.inner.sub.SubResourceMapper;
import org.folio.linked.data.mapper.resource.common.inner.sub.SubResourceMapperUnit;
import org.folio.linked.data.model.entity.Resource;

public interface WorkSubResourceMapperUnit extends SubResourceMapperUnit<Work> {

  @Override
  default Set<Class> getParentDto() {
    return Set.of(Work.class);
  }

  // required until we implement it for every Work mapper. Should be removed after.
  default Resource toEntity(Object dto, String predicate, SubResourceMapper subResourceMapper) {
    return null;
  }

}
