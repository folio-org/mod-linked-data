package org.folio.linked.data.util;

import static java.util.Optional.ofNullable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.function.Consumer;
import lombok.experimental.UtilityClass;
import org.folio.linked.data.domain.dto.Property;
import org.folio.linked.data.domain.dto.ProvisionActivity;
import org.folio.linked.data.domain.dto.Url;
import org.folio.linked.data.exception.JsonException;
import org.folio.linked.data.mapper.resource.inner.sub.SubResourceMapperResolver;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;

@UtilityClass
public class MappingUtil {

  public static Property toProperty(ObjectMapper mapper, Resource resource) {
    return readResourceDoc(mapper, resource, Property.class);
  }

  public static ProvisionActivity toProvisionActivity(ObjectMapper mapper, Resource resource) {
    return readResourceDoc(mapper, resource, ProvisionActivity.class);
  }

  public static Url toUrl(ObjectMapper mapper, Resource resource) {
    return readResourceDoc(mapper, resource, Url.class);
  }

  public static <T> T readResourceDoc(ObjectMapper mapper, Resource resource, Class<T> dtoClass) {
    try {
      var node = resource.getDoc() != null ? resource.getDoc() : mapper.createObjectNode();
      return mapper.treeToValue(node, dtoClass);
    } catch (JsonProcessingException e) {
      throw new JsonException(e.getMessage());
    }
  }

  public static void addMappedProperties(ObjectMapper mapper, Resource source, String predicate,
                                         Consumer<Property> consumer) {
    source.getOutgoingEdges().stream()
      .filter(re -> predicate.equals(re.getPredicate().getLabel()))
      .map(ResourceEdge::getTarget)
      .map(r -> toProperty(mapper, r))
      .forEach(consumer);
  }

  public static <T> void addMappedResources(ObjectMapper mapper, SubResourceMapperResolver subResourceMapperResolver,
                                            Resource resource, Consumer<T> consumer, Class<T> destination) {
    T item = readResourceDoc(mapper, resource, destination);
    resource.getOutgoingEdges().stream()
      .filter(re -> ofNullable(subResourceMapperResolver.getMapper(re.getPredicate().getLabel(),
        destination, false))
        .map(byPredicateMapper -> byPredicateMapper.toDto(re.getTarget(), item))
        .isEmpty())
      .map(ResourceEdge::getTarget)
      .forEach(r -> subResourceMapperResolver.getMapper(r.getType().getSimpleLabel(), destination, true)
        .toDto(r, item));
    consumer.accept(item);
  }
}
