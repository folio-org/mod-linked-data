package org.folio.linked.data.mapper.dto.monograph.instance.sub;

import java.util.Set;
import org.folio.linked.data.domain.dto.InstanceRequest;
import org.folio.linked.data.domain.dto.InstanceResponse;
import org.folio.linked.data.mapper.dto.common.SingleResourceMapperUnit;

public interface InstanceSubResourceMapperUnit extends SingleResourceMapperUnit {

  @Override
  default Set<Class<?>> supportedParents() {
    return Set.of(InstanceRequest.class, InstanceResponse.class);
  }
}
