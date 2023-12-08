package org.folio.linked.data.mapper.resource.monograph.instance.sub;

import java.util.Set;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.mapper.resource.common.sub.SubResourceMapperUnit;

public interface InstanceSubResourceMapperUnit extends SubResourceMapperUnit<Instance> {

  @Override
  default Set<Class<?>> getParentDto() {
    return Set.of(Instance.class);
  }
}
