package org.folio.linked.data.mapper.resource.common.inner.sub;

import org.folio.linked.data.model.entity.Resource;

public interface SubResourceMapperUnit<T> {

  T toDto(Resource source, T destination);

  Class<T> getParentDto();

  // required until we implement it for every Item and Work mappers. Should be declared as non default after.
  default Resource toEntity(Object dto, String predicate) {
    return null;
  }

}
