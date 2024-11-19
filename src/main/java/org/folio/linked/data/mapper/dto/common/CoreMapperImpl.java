package org.folio.linked.data.mapper.dto.common;

import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.folio.ld.dictionary.PropertyDictionary.LANGUAGE;
import static org.folio.ld.dictionary.PropertyDictionary.TARGET_AUDIENCE;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.folio.ld.dictionary.model.Predicate;
import org.folio.linked.data.domain.dto.WorkResponse;
import org.folio.linked.data.exception.RequestProcessingExceptionBuilder;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CoreMapperImpl implements CoreMapper {

  private final ObjectMapper jsonMapper;
  private final SingleResourceMapper singleResourceMapper;
  private final RequestProcessingExceptionBuilder exceptionBuilder;

  public CoreMapperImpl(ObjectMapper objectMapper,
                        @Lazy SingleResourceMapper singleResourceMapper,
                        RequestProcessingExceptionBuilder exceptionBuilder) {
    this.jsonMapper = objectMapper;
    this.singleResourceMapper = singleResourceMapper;
    this.exceptionBuilder = exceptionBuilder;
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
    addEdgeEntities(dtoList, parentEntity, predicate, parentDtoClass, false);
  }

  @Override
  public <T, P> void addIncomingEdges(@NonNull Resource parentEntity, @NonNull Class<P> parentDtoClass, List<T> dtoList,
                                      @NonNull Predicate predicate) {
    addEdgeEntities(dtoList, parentEntity, predicate, parentDtoClass, true);
  }

  private <T, P> void addEdgeEntities(List<T> dtoList, @NonNull Resource parentEntity,
                                      @NonNull Predicate predicate, @NonNull Class<P> parentDtoClass,
                                      boolean isIncoming) {
    ofNullable(dtoList)
      .stream()
      .flatMap(Collection::stream)
      .map(dto -> singleResourceMapper.toEntity(dto, parentDtoClass, predicate, parentEntity))
      .filter(
        r -> nonNull(r.getDoc()) || isNotEmpty(r.getIncomingEdges()) || isNotEmpty(r.getOutgoingEdges()))
      .map(r -> new ResourceEdge(isIncoming ? r : parentEntity,
        isIncoming ? parentEntity : r, predicate))
      .forEach(isIncoming ? parentEntity::addIncomingEdge : parentEntity::addOutgoingEdge);
  }

  private <T> T readResourceDoc(@NonNull Resource resource, @NonNull Class<T> dtoClass) {
    return readDoc(resource.getDoc(), dtoClass);
  }

  @Override
  public JsonNode toJson(Map<String, List<String>> map) {
    var node = jsonMapper.convertValue(map, JsonNode.class);
    return !(node instanceof NullNode) ? node : jsonMapper.createObjectNode();
  }

  private <T> T readDoc(JsonNode node, Class<T> dtoClass) {
    try {
      if (nonNull(node)) {
        if (dtoClass == WorkResponse.class) {
          // Temp fix - below properties loaded through the Python ETL have values in text format
          // causing the deserialization to fail. Here remove such properties from node
          ((ObjectNode) node).remove(TARGET_AUDIENCE.getValue());
          ((ObjectNode) node).remove(LANGUAGE.getValue());
        }
        return jsonMapper.treeToValue(node, dtoClass);
      } else {
        return jsonMapper.treeToValue(jsonMapper.createObjectNode(), dtoClass);
      }
    } catch (JsonProcessingException e) {
      log.error("JsonProcessingException during doc mapping to [{}]", dtoClass);
      throw exceptionBuilder.mappingException(dtoClass.getSimpleName(), String.valueOf(node));
    }
  }

}
