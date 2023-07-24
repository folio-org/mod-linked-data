package org.folio.linked.data.mapper.resource.common.inner.sub;

import java.util.Set;
import org.folio.linked.data.model.entity.Resource;

public interface SubResourceMapperUnit<T> {

  T toDto(Resource source, T destination);

  Set<Class> getParentDto();

  Resource toEntity(Object dto, String predicate, SubResourceMapper subResourceMapper);

}
