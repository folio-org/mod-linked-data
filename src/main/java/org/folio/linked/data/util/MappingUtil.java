package org.folio.linked.data.util;

import static java.util.Objects.nonNull;
import static org.folio.linked.data.util.BibframeConstants.DATE_URL;
import static org.folio.linked.data.util.BibframeConstants.PLACE_COMPONENTS;
import static org.folio.linked.data.util.BibframeConstants.PLACE_PRED;
import static org.folio.linked.data.util.BibframeConstants.PROPERTY_ID;
import static org.folio.linked.data.util.BibframeConstants.PROPERTY_LABEL;
import static org.folio.linked.data.util.BibframeConstants.PROPERTY_URI;
import static org.folio.linked.data.util.BibframeConstants.SIMPLE_AGENT_PRED;
import static org.folio.linked.data.util.BibframeConstants.SIMPLE_DATE_PRED;
import static org.folio.linked.data.util.BibframeConstants.SIMPLE_PLACE_PRED;
import static org.folio.linked.data.util.Constants.ERROR_JSON_PROCESSING;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.google.common.hash.Hashing;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;
import lombok.experimental.UtilityClass;
import org.folio.linked.data.domain.dto.Property;
import org.folio.linked.data.domain.dto.ProvisionActivity;
import org.folio.linked.data.domain.dto.Url;
import org.folio.linked.data.exception.JsonException;
import org.folio.linked.data.mapper.resource.common.inner.sub.SubResourceMapper;
import org.folio.linked.data.model.entity.Predicate;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.model.entity.ResourceType;
import org.folio.linked.data.service.dictionary.DictionaryService;

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

  public static <T> void addMappedResources(ObjectMapper mapper, SubResourceMapper subResourceMapper,
    Resource resource, Consumer<T> consumer, Class<T> destination) {
    T item = readResourceDoc(mapper, resource, destination);
    resource.getOutgoingEdges().forEach(re -> subResourceMapper.toDto(re, item));
    consumer.accept(item);
  }

  public static long hash(Resource resource, ObjectMapper mapper) {
    var serialized = resourceToJson(resource, mapper).toString();
    return Hashing.murmur3_32_fixed().hashString(serialized, StandardCharsets.UTF_8).padToLong();
  }

  public static JsonNode toJson(Object object, ObjectMapper mapper) {
    try {
      JsonNode node;
      if (object instanceof String string) {
        node = mapper.readTree(string);
      } else if (object instanceof Map map) {
        node = mapper.convertValue(map, JsonNode.class);
      } else if (object instanceof Resource resource) {
        return resourceToJson(resource, mapper);
      } else {
        node = mapper.valueToTree(object);
      }
      return !(node instanceof NullNode) ? node : mapper.createObjectNode();
    } catch (JsonProcessingException e) {
      throw new JsonException(ERROR_JSON_PROCESSING, e);
    }
  }

  public static <T> void mapResourceEdges(List<T> targets, Resource source,
    Supplier<Predicate> predicateSupplier, BiFunction<T, String, Resource> mappingFunction) {
    if (nonNull(targets)) {
      targets.forEach(target -> {
        var edge = new ResourceEdge()
          .setPredicate(predicateSupplier.get())
          .setSource(source)
          .setTarget(mappingFunction.apply(target, predicateSupplier.get().getLabel()));
        source.getOutgoingEdges().add(edge);
      });
    }
  }

  public static void mapPropertyEdges(List<Property> subProperties, Resource source,
    Supplier<Predicate> predicateSupplier, Supplier<ResourceType> typeSupplier, ObjectMapper mapper) {
    if (nonNull(subProperties)) {
      subProperties.forEach(property -> {
        var edge = new ResourceEdge()
          .setPredicate(predicateSupplier.get())
          .setSource(source)
          .setTarget(propertyToEntity(property, typeSupplier.get(), mapper));
        source.getOutgoingEdges().add(edge);
      });
    }
  }

  public static Resource propertyToEntity(Property property, ResourceType resourceType, ObjectMapper mapper) {
    var resource = new Resource();
    resource.setLabel(property.getLabel());
    resource.setType(resourceType);
    resource.setDoc(propertyToDoc(property, mapper));
    resource.setResourceHash(hash(resource, mapper));
    return resource;
  }

  public static Resource provisionActivityToEntity(ProvisionActivity dto, String label, ResourceType resourceType,
    DictionaryService<Predicate> predicateService, DictionaryService<ResourceType> typeHolder, ObjectMapper mapper) {
    Resource resource = null;
    if (nonNull(dto)) {
      resource = new Resource();
      resource.setLabel(label);
      resource.setType(resourceType);
      resource.setDoc(toJson(getProvisionActivityDoc(dto), mapper));
      mapPropertyEdges(dto.getPlace(), resource, () -> predicateService.get(PLACE_PRED),
        () -> typeHolder.get(PLACE_COMPONENTS), mapper);
      resource.setResourceHash(hash(resource, mapper));
    }
    return resource;
  }

  private Map<String, List<String>> getProvisionActivityDoc(ProvisionActivity dto) {
    var map = new HashMap<String, List<String>>();
    map.put(DATE_URL, dto.getDate());
    map.put(SIMPLE_AGENT_PRED, dto.getSimpleAgent());
    map.put(SIMPLE_DATE_PRED, dto.getSimpleDate());
    map.put(SIMPLE_PLACE_PRED, dto.getSimplePlace());
    return map;
  }

  private JsonNode propertyToDoc(Property property, ObjectMapper mapper) {
    var map = new HashMap<String, String>();
    map.put(PROPERTY_ID, property.getId());
    map.put(PROPERTY_LABEL, property.getLabel());
    map.put(PROPERTY_URI, property.getUri());
    return toJson(map, mapper);
  }

  private static JsonNode resourceToJson(Resource res, ObjectMapper mapper) {
    if (res.getDoc() != null && !res.getDoc().isEmpty()) {
      return res.getDoc();
    } else {
      var node = mapper.createObjectNode();
      res.getOutgoingEdges().forEach(edge -> {
        var predicate = edge.getPredicate().getLabel();
        if (node.has(predicate)) {
          if (node.get(predicate) instanceof ArrayNode array) {
            array.add(resourceToJson(edge.getTarget(), mapper));
          }
        } else {
          var array = mapper.createArrayNode();
          array.add(resourceToJson(edge.getTarget(), mapper));
          node.set(predicate, array);
        }
      });
      return node;
    }
  }
}
