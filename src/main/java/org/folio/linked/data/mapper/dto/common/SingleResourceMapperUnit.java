package org.folio.linked.data.mapper.dto.common;

import java.util.Set;
import org.folio.ld.dictionary.model.Predicate;
import org.folio.linked.data.model.entity.Resource;

public interface SingleResourceMapperUnit {

  <P> P toDto(Resource resourceToConvert, P parentDto, ResourceMappingContext context);

  Resource toEntity(Object dto, Resource parentEntity);

  Set<Class<?>> supportedParents();

  record ResourceMappingContext(Resource parentResource, Predicate predicate) {
  }
}
