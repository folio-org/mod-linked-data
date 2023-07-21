package org.folio.linked.data.mapper.resource.common;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import lombok.NonNull;
import org.apache.commons.lang3.function.TriFunction;
import org.folio.linked.data.domain.dto.PersonField;
import org.folio.linked.data.domain.dto.Property;
import org.folio.linked.data.domain.dto.ProvisionActivity;
import org.folio.linked.data.domain.dto.Url;
import org.folio.linked.data.mapper.resource.common.inner.sub.SubResourceMapper;
import org.folio.linked.data.mapper.resource.common.inner.sub.SubResourceMapperUnit;
import org.folio.linked.data.model.entity.Resource;

public interface CoreMapper {

  Property toProperty(@NonNull Resource resource);

  ProvisionActivity toProvisionActivity(@NonNull Resource resource);

  Url toUrl(@NonNull Resource resource);

  <T> void mapWithResources(@NonNull SubResourceMapper subResourceMapper, @NonNull Resource resource,
                            @NonNull Consumer<T> consumer, @NonNull Class<T> destination);

  <T> void addMappedResources(@NonNull SubResourceMapperUnit<T> subResourceMapperUnit, @NonNull Resource resource,
                              @NonNull String predicate, @NonNull T destination);

  void addMappedProperties(@NonNull Resource s, @NonNull String pred, @NonNull Consumer<Property> consumer);

  <T> T readResourceDoc(@NonNull Resource resource, @NonNull Class<T> dtoClass);

  void addMappedPersonLookups(@NonNull Resource source, @NonNull String predicate,
                              @NonNull Consumer<PersonField> personConsumer);

  long hash(@NonNull Resource resource);

  JsonNode toJson(Object object);

  <T> void mapResourceEdges(List<T> dtoList, @NonNull Resource source, String type, @NonNull String predicate,
                            @NonNull BiFunction<T, String, Resource> mappingFunction);

  <T, P> void mapResourceEdges(List<T> dtoList, @NonNull Resource source, @NonNull String predicate,
                               @NonNull Class<P> parentClass,
                               @NonNull TriFunction<T, String, Class<P>, Resource> mapping);

  void mapPropertyEdges(List<Property> subProperties, @NonNull Resource source, @NonNull String predicate,
                        @NonNull String resourceType);

  Resource propertyToEntity(@NonNull Property property, String resourceType);

  Resource provisionActivityToEntity(@NonNull ProvisionActivity dto, String label, @NonNull String resourceType);

}
