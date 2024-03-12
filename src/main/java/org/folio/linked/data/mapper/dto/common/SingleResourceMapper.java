package org.folio.linked.data.mapper.dto.common;

import java.util.Optional;
import lombok.NonNull;
import org.folio.ld.dictionary.model.Predicate;
import org.folio.linked.data.model.entity.Resource;

public interface SingleResourceMapper {

  <P> P toDto(@NonNull Resource source, @NonNull P parentDto, Resource parentResource, Predicate predicate);

  <P> Resource toEntity(@NonNull Object dto, @NonNull Class<P> parentDtoClass, Predicate predicate,
                        Resource parentEntity);

  Optional<SingleResourceMapperUnit> getMapperUnit(String typeUri, Predicate pred, Class<?> parentDto,
                                                   Class<?> dto);
}
