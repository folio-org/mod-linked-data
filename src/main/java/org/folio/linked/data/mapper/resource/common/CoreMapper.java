package org.folio.linked.data.mapper.resource.common;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import lombok.NonNull;
import org.apache.commons.lang3.function.TriFunction;
import org.folio.ld.dictionary.api.Predicate;
import org.folio.linked.data.mapper.resource.common.inner.sub.SubResourceMapper;
import org.folio.linked.data.mapper.resource.common.inner.sub.SubResourceMapperUnit;
import org.folio.linked.data.model.entity.Resource;

public interface CoreMapper {

  <T> void mapWithResources(@NonNull SubResourceMapper subResourceMapper, @NonNull Resource resource,
                            @NonNull Consumer<T> consumer, @NonNull Class<T> destination);

  <T> void addMappedResources(@NonNull SubResourceMapperUnit<T> subResourceMapperUnit, @NonNull Resource resource,
                              @NonNull Predicate predicate, @NonNull T destination);

  <T> T readResourceDoc(@NonNull Resource resource, @NonNull Class<T> dtoClass);

  long hash(@NonNull Resource resource);

  JsonNode toJson(Object object);

  <T> void mapSubEdges(List<T> dtoList, @NonNull Resource source, @NonNull Predicate predicate,
                       @NonNull Function<T, Resource> mappingFunction);

  <T, P> void mapInnerEdges(List<T> dtoList, @NonNull Resource source, @NonNull Predicate predicate,
                            @NonNull Class<P> parentClass,
                            @NonNull TriFunction<T, Predicate, Class<P>, Resource> mapping);
}
