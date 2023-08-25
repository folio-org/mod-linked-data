package org.folio.linked.data.mapper.resource.monograph.inner.instance.sub;

import java.util.Set;
import org.folio.linked.data.domain.dto.Instance2;
import org.folio.linked.data.mapper.resource.common.inner.sub.SubResourceMapperUnit;

public interface Instance2SubResourceMapperUnit extends SubResourceMapperUnit<Instance2> {

  @Override
  default Set<Class> getParentDto() {
    return Set.of(Instance2.class);
  }
}
