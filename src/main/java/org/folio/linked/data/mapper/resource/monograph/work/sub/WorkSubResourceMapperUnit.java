package org.folio.linked.data.mapper.resource.monograph.work.sub;

import java.util.Set;
import org.folio.linked.data.domain.dto.Work;
import org.folio.linked.data.mapper.resource.common.sub.SubResourceMapperUnit;

public interface WorkSubResourceMapperUnit extends SubResourceMapperUnit<Work> {
  @Override
  default Set<Class> getParentDto() {
    return Set.of(Work.class);
  }
}
