package org.folio.linked.data.mapper.resource.common.inner.sub;

import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;

public interface SubResourceMapper {

  <T> void toDto(ResourceEdge source, T destination);

  Resource toEntity(Object dto, String predicate);
}
