package org.folio.linked.data.mapper.kafka.impl;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.joining;
import static org.folio.ld.dictionary.PredicateDictionary.MAP;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.ld.dictionary.model.Predicate;
import org.folio.linked.data.mapper.dto.common.SingleResourceMapper;
import org.folio.linked.data.mapper.kafka.KafkaSearchMessageMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.model.entity.ResourceTypeEntity;
import org.jetbrains.annotations.NotNull;

@Log4j2
@RequiredArgsConstructor
public abstract class AbstractKafkaMessageMapper<T, I> implements KafkaSearchMessageMapper<T> {

  private static final String MSG_UNKNOWN_TYPES =
    "Unknown type(s) [{}] of [{}] was ignored during Resource [resourceId = {}] conversion to BibframeIndex message";

  private final SingleResourceMapper singleResourceMapper;

  List<I> extractIdentifiers(Resource resource) {
    return resource.getOutgoingEdges()
      .stream()
      .filter(re -> MAP.getUri().equals(re.getPredicate().getUri()))
      .map(ResourceEdge::getTarget)
      .map(this::mapToIdentifier)
      .flatMap(Optional::stream)
      .distinct()
      .toList();
  }

  abstract Optional<I> mapToIdentifier(Resource resource);

  String getValue(JsonNode doc, String... values) {
    if (nonNull(doc)) {
      for (String value : values) {
        if (doc.has(value) && !doc.get(value).isEmpty()) {
          return doc.get(value).get(0).asText();
        }
      }
    }
    return null;
  }

  <E extends Enum<E>> E toType(Resource resource,
                               Function<String, E> typeSupplier,
                               Class<E> enumClass,
                               Predicate predicate,
                               Class<?> parentDto) {
    if (isNull(resource.getTypes())) {
      return null;
    }
    return resource.getTypes()
      .stream()
      .map(ResourceTypeEntity::getUri)
      .filter(type -> singleResourceMapper.getMapperUnit(type, predicate, parentDto, null).isPresent())
      .findFirst()
      .map(typeUri -> typeUri.substring(typeUri.lastIndexOf("/") + 1))
      .map(typeUri -> {
        try {
          return typeSupplier.apply(typeUri);
        } catch (IllegalArgumentException ignored) {
          return null;
        }
      })
      .orElseGet(() -> {
        var enumNameWithParent = getTypeEnumNameWithParent(enumClass);
        log.warn(MSG_UNKNOWN_TYPES,
          resource.getTypes().stream().map(ResourceTypeEntity::getUri).collect(joining(", ")),
          enumNameWithParent, resource.getId());
        return null;
      });
  }

  @NotNull
  private <E extends Enum<E>> String getTypeEnumNameWithParent(Class<E> enumClass) {
    return enumClass.getName().substring(enumClass.getName().lastIndexOf(".") + 1);
  }
}
