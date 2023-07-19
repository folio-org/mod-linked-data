package org.folio.linked.data.mapper.resource.common;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.StreamSupport.stream;
import static org.folio.linked.data.util.BibframeConstants.DATE_URL;
import static org.folio.linked.data.util.BibframeConstants.PLACE_COMPONENTS;
import static org.folio.linked.data.util.BibframeConstants.PLACE_PRED;
import static org.folio.linked.data.util.BibframeConstants.PROPERTY_ID;
import static org.folio.linked.data.util.BibframeConstants.PROPERTY_LABEL;
import static org.folio.linked.data.util.BibframeConstants.PROPERTY_URI;
import static org.folio.linked.data.util.BibframeConstants.SAME_AS_PRED;
import static org.folio.linked.data.util.BibframeConstants.SIMPLE_AGENT_PRED;
import static org.folio.linked.data.util.BibframeConstants.SIMPLE_DATE_PRED;
import static org.folio.linked.data.util.BibframeConstants.SIMPLE_PLACE_PRED;
import static org.folio.linked.data.util.Constants.ERROR_JSON_PROCESSING;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.function.TriFunction;
import org.folio.linked.data.domain.dto.Lookup;
import org.folio.linked.data.domain.dto.Person;
import org.folio.linked.data.domain.dto.PersonField;
import org.folio.linked.data.domain.dto.Property;
import org.folio.linked.data.domain.dto.ProvisionActivity;
import org.folio.linked.data.domain.dto.Url;
import org.folio.linked.data.exception.JsonException;
import org.folio.linked.data.mapper.resource.common.inner.sub.SubResourceMapper;
import org.folio.linked.data.mapper.resource.common.inner.sub.SubResourceMapperUnit;
import org.folio.linked.data.model.entity.Predicate;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.model.entity.ResourceType;
import org.folio.linked.data.service.dictionary.DictionaryService;
import org.folio.linked.data.util.HashUtil;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CoreMapperImpl implements CoreMapper {

  private final DictionaryService<ResourceType> resourceTypeService;
  private final DictionaryService<Predicate> predicateService;
  private final ObjectMapper mapper;

  @Override
  public Property toProperty(@NonNull Resource resource) {
    return readResourceDoc(resource, Property.class);
  }

  @Override
  public ProvisionActivity toProvisionActivity(@NonNull Resource resource) {
    return readResourceDoc(resource, ProvisionActivity.class);
  }

  @Override
  public Url toUrl(@NonNull Resource resource) {
    return readResourceDoc(resource, Url.class);
  }

  @Override
  public <T> void mapWithResources(@NonNull SubResourceMapper subResourceMapper, @NonNull Resource resource,
                                   @NonNull Consumer<T> consumer, @NonNull Class<T> destination) {
    T item = readResourceDoc(resource, destination);
    resource.getOutgoingEdges().forEach(re -> subResourceMapper.toDto(re, item));
    consumer.accept(item);
  }

  public <T> void addMappedResources(@NonNull SubResourceMapperUnit<T> subResourceMapperUnit, @NonNull Resource source,
                                     @NonNull String predicate, @NonNull T destination) {
    source.getOutgoingEdges().stream()
      .filter(re -> re.getPredicate().getLabel().equals(predicate))
      .map(ResourceEdge::getTarget)
      .forEach(r -> subResourceMapperUnit.toDto(r, destination));
  }

  @Override
  public void addMappedProperties(@NonNull Resource resource, @NonNull String predicate,
                                  @NonNull Consumer<Property> consumer) {
    resource.getOutgoingEdges().stream()
      .filter(re -> predicate.equals(re.getPredicate().getLabel()))
      .map(ResourceEdge::getTarget)
      .filter(r -> nonNull(r.getDoc()))
      .map(r -> readResourceDoc(r, Property.class))
      .forEach(consumer);
  }

  @Override
  public <T> T readResourceDoc(@NonNull Resource resource, @NonNull Class<T> dtoClass) {
    return readDoc(resource.getDoc(), dtoClass);
  }

  @Override
  public void addMappedPersonLookups(@NonNull Resource resource, @NonNull String predicate,
                                     @NonNull Consumer<PersonField> personConsumer) {
    var person = new Person();
    addMappedLookups(resource, predicate, person::addSameAsItem);
    if (nonNull(person.getSameAs())) {
      personConsumer.accept(new PersonField().person(person));
    }
  }

  @Override
  public long hash(@NonNull Resource resource) {
    var serialized = resourceToJson(resource);
    return HashUtil.hash(serialized);
  }

  @Override
  public JsonNode toJson(Object object) {
    try {
      JsonNode node;
      if (object instanceof String string) {
        node = mapper.readTree(string);
      } else if (object instanceof Map map) {
        node = mapper.convertValue(map, JsonNode.class);
      } else if (object instanceof Resource resource) {
        return resourceToJson(resource);
      } else {
        node = mapper.valueToTree(object);
      }
      return !(node instanceof NullNode) ? node : mapper.createObjectNode();
    } catch (JsonProcessingException e) {
      throw new JsonException(ERROR_JSON_PROCESSING, e);
    }
  }

  @Override
  public <T> void mapResourceEdges(List<T> dtoList, @NonNull Resource source, String type,
                                   @NonNull String predicateLabel,
                                   @NonNull BiFunction<T, String, Resource> mappingFunction) {
    if (nonNull(dtoList)) {
      var predicate = predicateService.get(predicateLabel);
      dtoList.stream()
        .map(dto -> mappingFunction.apply(dto, isNull(type) ? predicateLabel : type))
        .map(resource -> new ResourceEdge(source, resource, predicate))
        .forEach(source.getOutgoingEdges()::add);
    }
  }

  @Override
  public <T, P> void mapResourceEdges(List<T> dtoList, @NonNull Resource source, @NonNull String predicateLabel,
                                      @NonNull Class<P> parent,
                                      @NonNull TriFunction<T, String, Class<P>, Resource> mapping) {
    if (nonNull(dtoList)) {
      var predicate = predicateService.get(predicateLabel);
      dtoList.stream()
        .map(dto -> mapping.apply(dto, predicateLabel, parent))
        .map(resource -> new ResourceEdge(source, resource, predicate))
        .forEach(source.getOutgoingEdges()::add);
    }
  }

  @Override
  public void mapPropertyEdges(List<Property> subProperties, @NonNull Resource source, @NonNull String predicateLabel,
                               @NonNull String resourceType) {
    if (nonNull(subProperties)) {
      var predicate = predicateService.get(predicateLabel);
      subProperties.forEach(property -> {
        var edge = new ResourceEdge(source, propertyToEntity(property, resourceType), predicate);
        source.getOutgoingEdges().add(edge);
      });
    }
  }

  @Override
  public Resource propertyToEntity(@NonNull Property property, @NonNull String resourceType) {
    var resource = new Resource();
    resource.setLabel(nonNull(property.getLabel()) ? property.getLabel() : resourceType);
    resource.setType(resourceTypeService.get(resourceType));
    resource.setDoc(propertyToDoc(property));
    resource.setResourceHash(hash(resource));
    return resource;
  }

  @Override
  public Resource provisionActivityToEntity(@NonNull ProvisionActivity dto, String label,
                                            @NonNull String resourceType) {
    Resource resource = new Resource();
    resource.setLabel(nonNull(label) ? label : resourceType);
    resource.setType(resourceTypeService.get(resourceType));
    resource.setDoc(provisionActivityToDoc(dto));
    mapPropertyEdges(dto.getPlace(), resource, PLACE_PRED, PLACE_COMPONENTS);
    resource.setResourceHash(hash(resource));
    return resource;
  }

  private JsonNode provisionActivityToDoc(ProvisionActivity dto) {
    var map = new HashMap<String, List<String>>();
    map.put(DATE_URL, dto.getDate());
    map.put(SIMPLE_AGENT_PRED, dto.getSimpleAgent());
    map.put(SIMPLE_DATE_PRED, dto.getSimpleDate());
    map.put(SIMPLE_PLACE_PRED, dto.getSimplePlace());
    return toJson(map);
  }

  private JsonNode propertyToDoc(Property property) {
    var map = new HashMap<String, String>();
    map.put(PROPERTY_ID, property.getId());
    map.put(PROPERTY_LABEL, property.getLabel());
    map.put(PROPERTY_URI, property.getUri());
    return toJson(map);
  }

  private JsonNode resourceToJson(Resource res) {
    ObjectNode node;
    if (nonNull(res.getDoc()) && !res.getDoc().isEmpty()) {
      node = res.getDoc().deepCopy();
    } else {
      node = mapper.createObjectNode();
    }
    node.put(PROPERTY_LABEL, res.getLabel());
    node.put("type", res.getType().getTypeHash());
    res.getOutgoingEdges().forEach(edge -> {
      var predicate = edge.getPredicate().getLabel();
      if (!node.has(predicate)) {
        node.set(predicate, mapper.createArrayNode());
      }
      ((ArrayNode) node.get(predicate)).add(resourceToJson(edge.getTarget()));
    });
    return node;
  }

  private void addMappedLookups(Resource resource, String predicate, Consumer<Lookup> consumer) {
    resource.getOutgoingEdges().stream()
      .filter(resourceEdge -> predicate.equals(resourceEdge.getPredicate().getLabel()))
      .map(ResourceEdge::getTarget)
      .map(Resource::getDoc)
      .filter(Objects::nonNull)
      .map(docNode -> docNode.get(SAME_AS_PRED))
      .filter(Objects::nonNull)
      .map(sameAsNode -> readDoc(sameAsNode, ArrayNode.class))
      .map(ArrayNode::elements)
      .map(elementIterator -> (Iterable<JsonNode>) () -> elementIterator)
      .flatMap(iterable -> stream(iterable.spliterator(), false))
      .map(lookupNode -> readDoc(lookupNode, Lookup.class))
      .forEach(consumer);
  }

  private <T> T readDoc(JsonNode node, Class<T> dtoClass) {
    try {
      return mapper.treeToValue(nonNull(node) ? node : mapper.createObjectNode(), dtoClass);
    } catch (JsonProcessingException e) {
      throw new JsonException(e.getMessage());
    }
  }

}
