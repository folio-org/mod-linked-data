package org.folio.linked.data.mapper.resource.common;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import org.folio.linked.data.domain.dto.PersonField;
import org.folio.linked.data.domain.dto.Property;
import org.folio.linked.data.domain.dto.ProvisionActivity;
import org.folio.linked.data.domain.dto.Url;
import org.folio.linked.data.mapper.resource.common.inner.sub.SubResourceMapper;
import org.folio.linked.data.model.entity.Resource;

public interface CommonMapper {

  Property toProperty(Resource resource);

  ProvisionActivity toProvisionActivity(Resource resource);

  Url toUrl(Resource resource);

  <T> void addMappedResources(SubResourceMapper subResourceMapper, Resource resource,
    Consumer<T> consumer, Class<T> destination);

  void addMappedProperties(Resource s, String pred, Consumer<Property> consumer);

  <T> T readResourceDoc(Resource resource, Class<T> dtoClass);

  void addMappedPersonLookups(Resource source, String predicate, Consumer<PersonField> personConsumer);

  long hash(Resource resource);

  JsonNode toJson(Object object);

  <T> void mapResourceEdges(List<T> targets, Resource source, String predicate,
    BiFunction<T, String, Resource> mappingFunction);

  void mapPropertyEdges(List<Property> subProperties, Resource source, String predicate, String type);

  Resource propertyToEntity(Property property, String resourceType);

  Resource provisionActivityToEntity(ProvisionActivity dto, String label, String resourceType);
}
