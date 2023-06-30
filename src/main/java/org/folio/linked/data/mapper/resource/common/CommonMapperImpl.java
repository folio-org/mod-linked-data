package org.folio.linked.data.mapper.resource.common;

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
import com.google.common.hash.Hashing;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Lookup;
import org.folio.linked.data.domain.dto.Person;
import org.folio.linked.data.domain.dto.PersonField;
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
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommonMapperImpl implements CommonMapper {

  private final DictionaryService<ResourceType> resourceTypeService;
  private final DictionaryService<Predicate> predicateService;
  private final ObjectMapper mapper;

  @Override
  public Property toProperty(Resource resource) {
    return readResourceDoc(resource, Property.class);
  }

  @Override
  public ProvisionActivity toProvisionActivity(Resource resource) {
    return readResourceDoc(resource, ProvisionActivity.class);
  }

  @Override
  public Url toUrl(Resource resource) {
    return readResourceDoc(resource, Url.class);
  }

  @Override
  public <T> void addMappedResources(SubResourceMapper subResourceMapper, Resource resource,
                                     Consumer<T> consumer, Class<T> destination) {
    T item = readResourceDoc(resource, destination);
    resource.getOutgoingEdges().forEach(re -> subResourceMapper.toDto(re, item));
    consumer.accept(item);
  }

  @Override
  public void addMappedProperties(Resource s, String pred, Consumer<Property> consumer) {
    s.getOutgoingEdges().stream()
      .filter(re -> pred.equals(re.getPredicate().getLabel()))
      .map(ResourceEdge::getTarget)
      .map(r -> readResourceDoc(r, Property.class))
      .forEach(consumer);
  }

  @Override
  public <T> T readResourceDoc(Resource resource, Class<T> dtoClass) {
    return readDoc(resource.getDoc(), dtoClass);
  }

  @Override
  public void addMappedPersonLookups(Resource source, String predicate, Consumer<PersonField> personConsumer) {
    var person = new Person();
    addMappedLookups(source, predicate, person::addSameAsItem);
    if (nonNull(person.getSameAs())) {
      personConsumer.accept(new PersonField().person(person));
    }
  }

  @Override
  public long hash(Resource resource) {
    var serialized = resourceToJson(resource).toString();
    return Hashing.murmur3_32_fixed().hashString(serialized, StandardCharsets.UTF_8).padToLong();
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
  public <T> void mapResourceEdges(List<T> targets, Resource source, String predicate,
                                   BiFunction<T, String, Resource> mappingFunction) {
    if (nonNull(targets)) {
      targets.forEach(target -> {
        var edge = new ResourceEdge()
          .setPredicate(predicateService.get(predicate))
          .setSource(source)
          .setTarget(mappingFunction.apply(target, predicateService.get(predicate).getLabel()));
        source.getOutgoingEdges().add(edge);
      });
    }
  }

  @Override
  public void mapPropertyEdges(List<Property> subProperties, Resource source, String predicate, String type) {
    if (nonNull(subProperties)) {
      subProperties.forEach(property -> {
        var edge = new ResourceEdge()
          .setPredicate(predicateService.get(predicate))
          .setSource(source)
          .setTarget(propertyToEntity(property, type));
        source.getOutgoingEdges().add(edge);
      });
    }
  }

  @Override
  public Resource propertyToEntity(Property property, String resourceType) {
    var resource = new Resource();
    resource.setLabel(property.getLabel());
    resource.setType(resourceTypeService.get(resourceType));
    resource.setDoc(propertyToDoc(property));
    resource.setResourceHash(hash(resource));
    return resource;
  }

  @Override
  public Resource provisionActivityToEntity(ProvisionActivity dto, String label, String resourceType) {
    Resource resource = null;
    if (nonNull(dto)) {
      resource = new Resource();
      resource.setLabel(label);
      resource.setType(resourceTypeService.get(resourceType));
      resource.setDoc(provisionActivityToDoc(dto));
      mapPropertyEdges(dto.getPlace(), resource, PLACE_PRED, PLACE_COMPONENTS);
      resource.setResourceHash(hash(resource));
    }
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
    if (res.getDoc() != null && !res.getDoc().isEmpty()) {
      return res.getDoc();
    } else {
      var node = mapper.createObjectNode();
      res.getOutgoingEdges().forEach(edge -> {
        var predicate = edge.getPredicate().getLabel();
        if (node.has(predicate)) {
          if (node.get(predicate) instanceof ArrayNode array) {
            array.add(resourceToJson(edge.getTarget()));
          }
        } else {
          var array = mapper.createArrayNode();
          array.add(resourceToJson(edge.getTarget()));
          node.set(predicate, array);
        }
      });
      return node;
    }
  }

  private void addMappedLookups(Resource source, String predicate, Consumer<Lookup> consumer) {
    source.getOutgoingEdges().stream()
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
