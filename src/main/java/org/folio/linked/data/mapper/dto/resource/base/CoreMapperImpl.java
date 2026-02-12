package org.folio.linked.data.mapper.dto.resource.base;

import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.folio.linked.data.util.JsonUtils.JSON_MAPPER;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.folio.ld.dictionary.model.Predicate;
import org.folio.linked.data.exception.RequestProcessingExceptionBuilder;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.NullNode;

@Log4j2
@Component
public class CoreMapperImpl implements CoreMapper {

  private final SingleResourceMapper singleResourceMapper;
  private final RequestProcessingExceptionBuilder exceptionBuilder;

  public CoreMapperImpl(@Lazy SingleResourceMapper singleResourceMapper,
                        RequestProcessingExceptionBuilder exceptionBuilder) {
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
    var node = JSON_MAPPER.convertValue(map, JsonNode.class);
    return !(node instanceof NullNode) ? node : JSON_MAPPER.createObjectNode();
  }

  private <T> T readDoc(JsonNode node, Class<T> dtoClass) {
    try {
      if (nonNull(node)) {
        return JSON_MAPPER.treeToValue(node, dtoClass);
      } else {
        return JSON_MAPPER.treeToValue(JSON_MAPPER.createObjectNode(), dtoClass);
      }
    } catch (JacksonException e) {
      log.error("JacksonException during doc mapping to [{}]", dtoClass);
      throw exceptionBuilder.mappingException(dtoClass.getSimpleName(), String.valueOf(node));
    }
  }
}
