package org.folio.linked.data.mapper.resource.common;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.Map;
import lombok.NonNull;
import org.folio.ld.dictionary.model.Predicate;
import org.folio.linked.data.model.entity.Resource;

public interface CoreMapper {

  <D> D toDtoWithEdges(@NonNull Resource resource, @NonNull Class<D> dtoClass, boolean mapIncomingEdges);

  <T, P> void addOutgoingEdges(@NonNull Resource parentEntity, @NonNull Class<P> parentDtoClass, List<T> dtoList,
                               @NonNull Predicate predicate);

  <T, P> void addIncomingEdges(@NonNull Resource parentEntity, @NonNull Class<P> parentDtoClass, List<T> dtoList,
                               @NonNull Predicate predicate);

  long hash(@NonNull Resource resource);

  JsonNode toJson(Map<String, List<String>> map);

}
