package org.folio.linked.data.mapper.resource.common;

import static java.util.Objects.nonNull;
import static org.folio.ld.dictionary.PropertyDictionary.LABEL_RDF;
import static org.folio.linked.data.util.Constants.ERROR_JSON_PROCESSING;
import static org.folio.linked.data.util.Constants.TYPE;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.function.TriFunction;
import org.folio.ld.dictionary.api.Predicate;
import org.folio.linked.data.exception.JsonException;
import org.folio.linked.data.mapper.resource.common.sub.SubResourceMapper;
import org.folio.linked.data.mapper.resource.common.sub.SubResourceMapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.util.HashUtil;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CoreMapperImpl implements CoreMapper {

  private final ObjectMapper mapper;

  @Override
  public <T> void mapWithResources(@NonNull SubResourceMapper subResourceMapper, @NonNull Resource resource,
                                   @NonNull Consumer<T> consumer, @NonNull Class<T> destination) {
    T item = readResourceDoc(resource, destination);
    resource.getOutgoingEdges().forEach(re -> subResourceMapper.toDto(re, item));
    consumer.accept(item);
  }

  @Override
  public <T> void addMappedResources(@NonNull SubResourceMapperUnit<T> subResourceMapperUnit, @NonNull Resource source,
                                     @NonNull Predicate predicate, @NonNull T destination) {
    source.getOutgoingEdges().stream()
      .filter(re -> re.getPredicate().getUri().equals(predicate.getUri()))
      .map(ResourceEdge::getTarget)
      .forEach(r -> subResourceMapperUnit.toDto(r, destination));
  }

  @Override
  public <T> T readResourceDoc(@NonNull Resource resource, @NonNull Class<T> dtoClass) {
    return readDoc(resource.getDoc(), dtoClass);
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
  public <T> void mapSubEdges(List<T> dtoList, @NonNull Resource source,
                              @NonNull Predicate predicate,
                              @NonNull Function<T, Resource> mappingFunction) {
    if (nonNull(dtoList)) {
      dtoList.stream()
        .map(mappingFunction)
        .map(resource -> new ResourceEdge(source, resource, predicate))
        .forEach(source.getOutgoingEdges()::add);
    }
  }

  @Override
  public <T, P> void mapInnerEdges(List<T> dtoList, @NonNull Resource source, @NonNull Predicate predicate,
                                   @NonNull Class<P> parent,
                                   @NonNull TriFunction<T, Predicate, Class<P>, Resource> mapping) {
    if (nonNull(dtoList)) {
      dtoList.stream()
        .map(dto -> mapping.apply(dto, predicate, parent))
        .map(resource -> new ResourceEdge(source, resource, predicate))
        .forEach(source.getOutgoingEdges()::add);
    }
  }

  private JsonNode resourceToJson(Resource res) {
    ObjectNode node;
    if (nonNull(res.getDoc()) && !res.getDoc().isEmpty()) {
      node = res.getDoc().deepCopy();
    } else {
      node = mapper.createObjectNode();
    }
    node.put(LABEL_RDF.getValue(), res.getLabel());
    if (nonNull(res.getTypes())) {
      node.put(TYPE, res.getTypes().iterator().next().getHash());
    }
    res.getOutgoingEdges().forEach(edge -> {
      var predicate = edge.getPredicate().getUri();
      if (!node.has(predicate)) {
        node.set(predicate, mapper.createArrayNode());
      }
      ((ArrayNode) node.get(predicate)).add(resourceToJson(edge.getTarget()));
    });
    return node;
  }

  private <T> T readDoc(JsonNode node, Class<T> dtoClass) {
    try {
      return mapper.treeToValue(nonNull(node) ? node : mapper.createObjectNode(), dtoClass);
    } catch (JsonProcessingException e) {
      throw new JsonException(e.getMessage());
    }
  }

}
