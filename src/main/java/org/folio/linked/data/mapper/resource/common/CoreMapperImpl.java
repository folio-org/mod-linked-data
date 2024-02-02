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

  public <D> D toDtoWithEdges(@NonNull Resource resource, @NonNull Class<D> dtoClass, boolean mapIncomingEdges) {
    D dto = readResourceDoc(resource, dtoClass);
    resource.getOutgoingEdges()
      .forEach(re -> singleResourceMapper.toDto(re.getTarget(), dto, resource, re.getPredicate()));
    if (mapIncomingEdges) {
      resource.getIncomingEdges()
        .forEach(re -> singleResourceMapper.toDto(re.getSource(), dto, resource, re.getPredicate()));
    }
    return dto;
  }

  @Override
  public <T, P> void addOutgoingEdges(@NonNull Resource parentEntity, @NonNull Class<P> parentDtoClass, List<T> dtoList,
                                      @NonNull Predicate predicate) {
    addEdgeEntities(dtoList, parentEntity, predicate, parentDtoClass, true);
  }

  @Override
  public <T, P> void addIncomingEdges(@NonNull Resource parentEntity, @NonNull Class<P> parentDtoClass, List<T> dtoList,
                                      @NonNull Predicate predicate) {
    addEdgeEntities(dtoList, parentEntity, predicate, parentDtoClass, false);
  }

  private <T, P> void addEdgeEntities(List<T> dtoList, @NonNull Resource parentEntity,
                                      @NonNull Predicate predicate, @NonNull Class<P> parentDtoClass,
                                      boolean isOutgoingOrIncoming) {
    ofNullable(dtoList)
      .stream()
      .flatMap(Collection::stream)
      .map(dto -> singleResourceMapper.toEntity(dto, parentDtoClass, predicate, parentEntity))
      .filter(
        r -> nonNull(r.getDoc()) || isNotEmpty(isOutgoingOrIncoming ? r.getOutgoingEdges() : r.getIncomingEdges()))
      .map(r -> new ResourceEdge(isOutgoingOrIncoming ? parentEntity : r,
        isOutgoingOrIncoming ? r : parentEntity, predicate))
      .forEach((isOutgoingOrIncoming ? parentEntity.getOutgoingEdges() : parentEntity.getIncomingEdges())::add);
  }

  private <T> T readResourceDoc(@NonNull Resource resource, @NonNull Class<T> dtoClass) {
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
