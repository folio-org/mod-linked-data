package org.folio.linked.data.mapper.resource.common.sub;

import java.util.Optional;
import lombok.NonNull;
import org.folio.ld.dictionary.api.Predicate;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;

public interface SubResourceMapper {

  <D> void toDto(@NonNull ResourceEdge source, @NonNull D destination);

  <P> Resource toEntity(@NonNull Object dto, Predicate predicate, @NonNull Class<P> parentDtoClass);

  <T> Optional<SubResourceMapperUnit<T>> getMapperUnit(String typeUri, Predicate pred, Class<?> parentDto,
                                                       Class<?> dto);
}
