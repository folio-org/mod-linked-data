package org.folio.linked.data.mapper.dto.common;

import static java.util.Optional.ofNullable;
import static org.folio.ld.dictionary.PropertyDictionary.RESOURCE_PREFERRED;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Set;
import org.folio.linked.data.model.entity.Resource;

public interface SingleResourceMapperUnit {

  <P> P toDto(Resource source, P parentDto, Resource parentResource);

  Resource toEntity(Object dto, Resource parentEntity);

  Set<Class<?>> supportedParents();

  default boolean isPreferred(Resource resource) {
    return ofNullable(resource.getDoc())
      .map(doc -> doc.get(RESOURCE_PREFERRED.getValue()))
      .map(jsonNode -> jsonNode.get(0))
      .map(JsonNode::asBoolean)
      .orElse(false);
  }

}
