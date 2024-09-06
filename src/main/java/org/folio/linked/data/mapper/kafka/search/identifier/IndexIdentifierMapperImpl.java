package org.folio.linked.data.mapper.kafka.search.identifier;

import static java.util.Objects.isNull;
import static java.util.stream.Collectors.joining;
import static org.folio.ld.dictionary.PredicateDictionary.MAP;
import static org.folio.ld.dictionary.PropertyDictionary.EAN_VALUE;
import static org.folio.ld.dictionary.PropertyDictionary.LOCAL_ID_VALUE;
import static org.folio.ld.dictionary.PropertyDictionary.NAME;
import static org.folio.linked.data.domain.dto.LinkedDataIdentifier.TypeEnum;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ObjectUtils;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.domain.dto.LinkedDataIdentifier;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.model.entity.ResourceTypeEntity;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class IndexIdentifierMapperImpl implements IndexIdentifierMapper {

  private static final String MSG_UNKNOWN_TYPES =
    "Unknown type(s) [{}] was ignored during Resource [resourceId = {}] conversion to ResourceIndex message";

  private static final Collection<String> IDENTIFIER_PROPERTY_VALUES = List.of(
    NAME.getValue(),
    EAN_VALUE.getValue(),
    LOCAL_ID_VALUE.getValue()
  );

  @Override
  public List<LinkedDataIdentifier> extractIdentifiers(Resource resource) {
    return resource.getOutgoingEdges()
      .stream()
      .filter(re -> MAP.getUri().equals(re.getPredicate().getUri()))
      .map(ResourceEdge::getTarget)
      .map(this::mapToIdentifier)
      .flatMap(Optional::stream)
      .distinct()
      .toList();
  }

  private Optional<LinkedDataIdentifier> mapToIdentifier(Resource resource) {
    return Optional.of(resource)
      .map(Resource::getDoc)
      .flatMap(this::getValue)
      .map(s -> new LinkedDataIdentifier().value(s))
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

  private LinkedDataIdentifier appendType(Resource resource, LinkedDataIdentifier linkedDataIdentifier) {
    toType(resource)
      .ifPresent(linkedDataIdentifier::type);
    return linkedDataIdentifier;
  }

  private Optional<TypeEnum> toType(Resource resource) {
    if (isNull(resource.getTypes())) {
      return Optional.empty();
    }
    var type = resource.getTypes()
      .stream()
      .map(ResourceTypeEntity::getUri)
      .filter(uri -> ObjectUtils.notEqual(uri, ResourceTypeDictionary.IDENTIFIER.getUri()))
      .findFirst()
      .map(this::normalizeUri)
      .flatMap(this::getType);

    if (type.isEmpty()) {
      logError(resource);
    }
    return type;
  }

  private Optional<TypeEnum> getType(String typeUri) {
    try {
      return Optional.of(TypeEnum.fromValue(typeUri));
    } catch (IllegalArgumentException ignored) {
      return Optional.empty();
    }
  }

  private String normalizeUri(String typeUri) {
    return typeUri.substring(typeUri.lastIndexOf("/") + 1);
  }

  private void logError(Resource resource) {
    log.warn(MSG_UNKNOWN_TYPES,
      resource.getTypes().stream().map(ResourceTypeEntity::getUri).collect(joining(", ")), resource.getId());
  }
}
