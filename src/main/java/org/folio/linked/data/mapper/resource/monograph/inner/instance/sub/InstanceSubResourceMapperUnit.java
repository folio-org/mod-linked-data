package org.folio.linked.data.mapper.resource.monograph.inner.instance.sub;

import java.util.Set;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.mapper.resource.common.inner.sub.SubResourceMapperUnit;

public interface InstanceSubResourceMapperUnit extends SubResourceMapperUnit<Instance> {

  @Override
  default Set<Class> getParentDto() {
    return Set.of(Instance.class);
  }
}
