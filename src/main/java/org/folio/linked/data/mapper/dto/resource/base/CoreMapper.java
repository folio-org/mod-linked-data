package org.folio.linked.data.mapper.dto.resource.base;

import java.util.List;
import java.util.Map;
import lombok.NonNull;
import org.folio.ld.dictionary.model.Predicate;
import org.folio.linked.data.model.entity.Resource;
import tools.jackson.databind.JsonNode;

public interface CoreMapper {

  <D> D toDtoWithEdges(@NonNull Resource resource, @NonNull Class<D> dtoClass, boolean mapIncomingEdges);

  <T, P> void addOutgoingEdges(@NonNull Resource parentEntity, @NonNull Class<P> parentDtoClass, List<T> dtoList,
                               @NonNull Predicate predicate);

  <T, P> void addIncomingEdges(@NonNull Resource parentEntity, @NonNull Class<P> parentDtoClass, List<T> dtoList,
                               @NonNull Predicate predicate);

  JsonNode toJson(Map<String, List<String>> map);

}
