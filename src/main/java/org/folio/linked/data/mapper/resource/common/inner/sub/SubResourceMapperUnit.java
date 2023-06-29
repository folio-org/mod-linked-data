package org.folio.linked.data.mapper.resource.common.inner.sub;

import org.folio.linked.data.model.entity.Resource;

public interface SubResourceMapperUnit<T> {

  T toDto(Resource source, T destination);

  Class<T> getParentDto();

  default Resource toEntity(Object dto, String predicate) {
    return null;
  }

}
