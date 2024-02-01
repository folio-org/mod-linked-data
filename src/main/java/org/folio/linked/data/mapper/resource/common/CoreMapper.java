package org.folio.linked.data.mapper.resource.common;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import lombok.NonNull;
import org.folio.ld.dictionary.api.Predicate;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;

public interface CoreMapper {

  <T> void mapToDtoWithEdges(@NonNull Resource resource, @NonNull Consumer<T> consumer, @NonNull Class<T> destination);

  <T> void addMappedOutgoingResources(@NonNull SingleResourceMapperUnit singleResourceMapperUnit,
                                      @NonNull Resource resource, @NonNull Predicate predicate, @NonNull T destination);

  <T> void addMappedIncomingResources(@NonNull SingleResourceMapperUnit singleResourceMapperUnit,
                                      @NonNull Resource resource, @NonNull Predicate predicate, @NonNull T destination);

  <T> T readResourceDoc(@NonNull Resource resource, @NonNull Class<T> dtoClass);

  long hash(@NonNull Resource resource);

  JsonNode toJson(Object object);

  <T> void mapSubEdges(List<T> dtoList, @NonNull Resource source, @NonNull Predicate predicate,
                       @NonNull Function<T, Resource> mappingFunction);

  <T, P> List<ResourceEdge> toOutgoingEdges(List<T> dtoList, @NonNull Resource parentEntity,
                                            @NonNull Predicate predicate, @NonNull Class<P> parentDtoClass);

  <T, P> List<ResourceEdge> toIncomingEdges(List<T> dtoList, @NonNull Resource parentEntity,
                                            @NonNull Predicate predicate, @NonNull Class<P> parentDtoClass);

}
