package org.folio.linked.data.mapper.resource.common.inner.sub;

import lombok.NonNull;
import org.folio.ld.dictionary.api.Predicate;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;

public interface SubResourceMapper {

  <T> void toDto(@NonNull ResourceEdge source, @NonNull T destination);

  <P> Resource toEntity(@NonNull Object dto, Predicate predicate, @NonNull Class<P> parentDtoClass);
}
