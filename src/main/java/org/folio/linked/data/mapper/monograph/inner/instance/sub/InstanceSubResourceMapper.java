package org.folio.linked.data.mapper.monograph.inner.instance.sub;

import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.mapper.resource.inner.sub.SubResourceMapper;

public interface InstanceSubResourceMapper extends SubResourceMapper<Instance> {

  @Override
  default Class<Instance> destinationDtoClass() {
    return Instance.class;
  }
}
