package org.folio.linked.data.mapper.resource.inner.sub;

import org.folio.linked.data.model.entity.Resource;

public interface SubResourceMapper<T> {

  T toDto(Resource source, T destination);

  Class<T> destinationDtoClass();

}
