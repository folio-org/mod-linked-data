package org.folio.linked.data.mapper.kafka.impl;

import static java.util.Objects.isNull;
import static java.util.stream.Collectors.joining;
import static org.folio.ld.dictionary.PredicateDictionary.MAP;
import static org.folio.ld.dictionary.PropertyDictionary.EAN_VALUE;
import static org.folio.ld.dictionary.PropertyDictionary.LOCAL_ID_VALUE;
import static org.folio.ld.dictionary.PropertyDictionary.NAME;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ObjectUtils;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.mapper.kafka.IndexIdentifierMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.model.entity.ResourceTypeEntity;

@Log4j2
@RequiredArgsConstructor
public class IndexIdentifierMapperImpl<T, E extends Enum<E>> implements IndexIdentifierMapper<T> {

  private static final String MSG_UNKNOWN_TYPES =
    "Unknown type(s) [{}] of [{}] was ignored during Resource [resourceId = {}] conversion to BibframeIndex message";

  private static final Collection<String> IDENTIFIER_PROPERTY_VALUES = List.of(
    NAME.getValue(),
    EAN_VALUE.getValue(),
    LOCAL_ID_VALUE.getValue()
  );

  private final Class<?> parentDto;
  private final Class<? extends Enum<?>> enumClass;
  private final Function<String, E> typeSupplier;
  private final Function<String, T> indexCreateByValueFunction;
  private final BiFunction<T, E, T> indexUpdateTypeFunction;

  @Override
  public List<T> extractIdentifiers(Resource resource) {
    return resource.getOutgoingEdges()
      .stream()
      .filter(re -> MAP.getUri().equals(re.getPredicate().getUri()))
      .map(ResourceEdge::getTarget)
      .map(this::mapToIdentifier)
      .flatMap(Optional::stream)
      .distinct()
      .toList();
  }

  private Optional<T> mapToIdentifier(Resource resource) {
    return Optional.of(resource)
      .map(Resource::getDoc)
      .flatMap(this::getValue)
      .map(indexCreateByValueFunction)
      .map(t -> appendType(resource, t));
  }

  private Optional<String> getValue(JsonNode doc) {
    return IDENTIFIER_PROPERTY_VALUES.stream()
      .filter(doc::has)
      .map(doc::get)
      .filter(node -> !node.isEmpty())
      .map(value -> value.get(0))
      .map(JsonNode::asText)
      .findFirst();
  }

  private T appendType(Resource resource, T t) {
    return toType(resource)
      .map(type -> indexUpdateTypeFunction.apply(t, type))
      .orElse(t);
  }

  private Optional<E> toType(Resource resource) {
    if (isNull(resource.getTypes())) {
      return Optional.empty();
    }
    var type = resource.getTypes()
      .stream()
      .map(ResourceTypeEntity::getUri)
      .filter(uri -> ObjectUtils.notEqual(uri, ResourceTypeDictionary.IDENTIFIER.getUri()))
      .findFirst()
      .map(this::normalizeUri)
      .flatMap(this::getE);

    if (type.isEmpty()) {
      logError(resource);
    }
    return type;
  }

  private Optional<E> getE(String typeUri) {
    try {
      return Optional.of(typeSupplier.apply(typeUri));
    } catch (IllegalArgumentException ignored) {
      return Optional.empty();
    }
  }

  private String normalizeUri(String typeUri) {
    return typeUri.substring(typeUri.lastIndexOf("/") + 1);
  }

  private String getTypeEnumNameWithParent() {
    var startIdx = enumClass.getName().lastIndexOf(".") + 1;
    return enumClass.getName()
      .substring(startIdx);
  }

  private void logError(Resource resource) {
    var enumNameWithParent = getTypeEnumNameWithParent();
    log.warn(MSG_UNKNOWN_TYPES,
      resource.getTypes().stream().map(ResourceTypeEntity::getUri).collect(joining(", ")),
      enumNameWithParent, resource.getId());
  }
}
