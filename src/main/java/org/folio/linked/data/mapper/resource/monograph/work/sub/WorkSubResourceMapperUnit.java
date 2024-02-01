package org.folio.linked.data.mapper.resource.monograph.work.sub;

import java.util.Set;
import org.folio.linked.data.domain.dto.Work;
import org.folio.linked.data.domain.dto.WorkReference;
import org.folio.linked.data.mapper.resource.common.SingleResourceMapperUnit;

public interface WorkSubResourceMapperUnit extends SingleResourceMapperUnit {
  @Override
  default Set<Class<?>> supportedParents() {
    return Set.of(Work.class, WorkReference.class);
  }
}
