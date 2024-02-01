package org.folio.linked.data.mapper.resource.common;

import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.folio.ld.dictionary.PropertyDictionary.LABEL_RDF;
import static org.folio.linked.data.util.Constants.ERROR_JSON_PROCESSING;
import static org.folio.linked.data.util.Constants.TYPE;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import lombok.NonNull;
import org.folio.ld.dictionary.api.Predicate;
import org.folio.linked.data.exception.JsonException;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.util.HashUtil;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class CoreMapperImpl implements CoreMapper {

  private final ObjectMapper jsonMapper;
  private final SingleResourceMapper singleResourceMapper;

  public CoreMapperImpl(ObjectMapper objectMapper, @Lazy SingleResourceMapper singleResourceMapper) {
    this.jsonMapper = objectMapper;
    this.singleResourceMapper = singleResourceMapper;
  }

  public <T> void mapToDtoWithEdges(@NonNull Resource resource, @NonNull Consumer<T> consumer,
                                    @NonNull Class<T> destination) {
    T item = readResourceDoc(resource, destination);
    resource.getOutgoingEdges()
      .forEach(re -> singleResourceMapper.toDto(re.getTarget(), item, resource, re.getPredicate()));
    consumer.accept(item);
  }

  @Override
  public <T> void addMappedOutgoingResources(@NonNull SingleResourceMapperUnit singleResourceMapperUnit,
                                             @NonNull Resource source, @NonNull Predicate predicate,
                                             @NonNull T destination) {
    source.getOutgoingEdges().stream()
      .filter(re -> re.getPredicate().getUri().equals(predicate.getUri()))
      .map(ResourceEdge::getTarget)
      .forEach(r -> singleResourceMapperUnit.toDto(r, destination, null));
  }

  @Override
  public <T> void addMappedIncomingResources(@NonNull SingleResourceMapperUnit singleResourceMapperUnit,
                                             @NonNull Resource source, @NonNull Predicate predicate,
                                             @NonNull T destination) {
    source.getIncomingEdges().stream()
      .filter(re -> re.getPredicate().getUri().equals(predicate.getUri()))
      .map(ResourceEdge::getSource)
      .forEach(r -> singleResourceMapperUnit.toDto(r, destination, null));
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
        node = jsonMapper.readTree(string);
      } else if (object instanceof Map<?, ?> map) {
        node = jsonMapper.convertValue(map, JsonNode.class);
      } else if (object instanceof Resource resource) {
        return resourceToJson(resource);
      } else {
        node = jsonMapper.valueToTree(object);
      }
      return !(node instanceof NullNode) ? node : jsonMapper.createObjectNode();
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
        .filter(r -> nonNull(r.getDoc()) || isNotEmpty(r.getOutgoingEdges()))
        .map(r -> new ResourceEdge(source, r, predicate))
        .forEach(source.getOutgoingEdges()::add);
    }
  }

  @Override
  public <T, P> List<ResourceEdge> toOutgoingEdges(List<T> dtoList, @NonNull Resource parentEntity,
                                                   @NonNull Predicate predicate,
                                                   @NonNull Class<P> parentDtoClass) {
    return ofNullable(dtoList)
      .stream()
      .flatMap(Collection::stream)
      .map(dto -> singleResourceMapper.toEntity(dto, parentDtoClass, predicate, parentEntity))
      .filter(r -> nonNull(r.getDoc()) || isNotEmpty(r.getOutgoingEdges()))
      .map(resource -> {
        var edge = new ResourceEdge(parentEntity, resource, predicate);
        parentEntity.getOutgoingEdges().add(edge);
        return edge;
      })
      .toList();
  }

  @Override
  public <T, P> List<ResourceEdge> toIncomingEdges(List<T> dtoList, @NonNull Resource parentEntity,
                                                   @NonNull Predicate predicate,
                                                   @NonNull Class<P> parentDtoClass) {
    return ofNullable(dtoList)
      .stream()
      .flatMap(Collection::stream)
      .map(dto -> singleResourceMapper.toEntity(dto, parentDtoClass, predicate, parentEntity))
      .filter(r -> nonNull(r.getDoc()) || isNotEmpty(r.getOutgoingEdges()))
      .map(resource -> {
        var edge = new ResourceEdge(parentEntity, resource, predicate);
        parentEntity.getIncomingEdges().add(edge);
        return edge;
      })
      .toList();
  }

  private JsonNode resourceToJson(Resource res) {
    ObjectNode node;
    if (nonNull(res.getDoc()) && !res.getDoc().isEmpty()) {
      node = res.getDoc().deepCopy();
    } else {
      node = jsonMapper.createObjectNode();
    }
    node.put(LABEL_RDF.getValue(), res.getLabel());
    if (nonNull(res.getTypes())) {
      node.put(TYPE, res.getTypes().iterator().next().getHash());
    }
    res.getOutgoingEdges().forEach(edge -> {
      var predicate = edge.getPredicate().getUri();
      if (!node.has(predicate)) {
        node.set(predicate, jsonMapper.createArrayNode());
      }
      ((ArrayNode) node.get(predicate)).add(resourceToJson(edge.getTarget()));
    });
    return node;
  }

  private <T> T readDoc(JsonNode node, Class<T> dtoClass) {
    try {
      return jsonMapper.treeToValue(nonNull(node) ? node : jsonMapper.createObjectNode(), dtoClass);
    } catch (JsonProcessingException e) {
      throw new JsonException(e.getMessage());
    }
  }

}
