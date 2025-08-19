package org.folio.linked.data.mapper.dto.resource.common.work.sub;

import java.util.Set;
import org.folio.linked.data.domain.dto.WorkRequest;
import org.folio.linked.data.domain.dto.WorkResponse;
import org.folio.linked.data.mapper.dto.resource.base.SingleResourceMapperUnit;

public interface WorkSubResourceMapperUnit extends SingleResourceMapperUnit {

  @Override
  default Set<Class<?>> supportedParents() {
    return Set.of(WorkRequest.class, WorkResponse.class);
  }
}
