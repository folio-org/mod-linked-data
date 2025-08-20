package org.folio.linked.data.mapper.dto.resource.base;

import lombok.NonNull;
import org.folio.ld.dictionary.model.Predicate;
import org.folio.linked.data.model.entity.Resource;

public interface SingleResourceMapper {

  <P> P toDto(@NonNull Resource source, @NonNull P parentDto, Resource parentResource, Predicate predicate);

  <P> Resource toEntity(@NonNull Object dto, @NonNull Class<P> parentRequestDto, Predicate predicate,
                        Resource parentEntity);
}
