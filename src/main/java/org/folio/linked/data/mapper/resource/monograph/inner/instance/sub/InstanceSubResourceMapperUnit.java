package org.folio.linked.data.mapper.resource.monograph.inner.instance.sub;

import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.mapper.resource.common.inner.sub.SubResourceMapperUnit;

public interface InstanceSubResourceMapperUnit extends SubResourceMapperUnit<Instance> {

  @Override
  default Class<Instance> getParentDto() {
    return Instance.class;
  }
}
