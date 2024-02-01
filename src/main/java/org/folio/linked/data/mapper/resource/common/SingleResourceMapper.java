package org.folio.linked.data.mapper.resource.common;

import java.util.Optional;
import lombok.NonNull;
import org.folio.ld.dictionary.api.Predicate;
import org.folio.linked.data.model.entity.Resource;

public interface SingleResourceMapper {

  <D> D toDto(@NonNull Resource source, @NonNull D parentDto, Resource parentResource, Predicate predicate);

  <P> Resource toEntity(@NonNull Object dto, @NonNull Class<P> parentDtoClass, Predicate predicate,
                        Resource parentEntity);

  Optional<SingleResourceMapperUnit> getMapperUnit(String typeUri, Predicate pred, Class<?> parentDto,
                                                   Class<?> dto);
}
