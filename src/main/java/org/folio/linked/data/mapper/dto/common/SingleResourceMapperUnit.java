package org.folio.linked.data.mapper.dto.common;

import java.util.Set;
import org.folio.linked.data.model.entity.Resource;

public interface SingleResourceMapperUnit {

  <P> P toDto(Resource source, P parentDto, Resource parentResource);

  Resource toEntity(Object dto, Resource parentEntity);

  Set<Class<?>> supportedParents();

}
