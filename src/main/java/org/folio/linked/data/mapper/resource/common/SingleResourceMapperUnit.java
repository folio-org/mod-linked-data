package org.folio.linked.data.mapper.resource.common;

import java.util.Set;
import org.folio.linked.data.model.entity.Resource;

public interface SingleResourceMapperUnit {

  <T> T toDto(Resource source, T parentDto, Resource parentResource);

  Resource toEntity(Object dto, Resource parentEntity);

  Set<Class<?>> supportedParents();

}
