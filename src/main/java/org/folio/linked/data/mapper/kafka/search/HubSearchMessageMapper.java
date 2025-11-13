package org.folio.linked.data.mapper.kafka.search;

import static java.lang.String.join;
import static org.apache.commons.lang3.StringUtils.SPACE;
import static org.folio.ld.dictionary.PredicateDictionary.CREATOR;
import static org.folio.linked.data.util.Constants.SEARCH_HUB_RESOURCE_NAME;
import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import java.util.UUID;
import org.folio.linked.data.domain.dto.LinkedDataHub;
import org.folio.linked.data.domain.dto.ResourceIndexEvent;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = SPRING, imports = UUID.class)
public abstract class HubSearchMessageMapper {

  @Mapping(target = "id", expression = "java(UUID.randomUUID().toString())")
  @Mapping(target = "resourceName", constant = SEARCH_HUB_RESOURCE_NAME)
  @Mapping(target = "_new", expression = "java(toLinkedDataHub(resource))")
  public abstract ResourceIndexEvent toIndex(Resource resource);

  @Mapping(target = "label", expression = "java(toHubLabel(resource))")
  protected abstract LinkedDataHub toLinkedDataHub(Resource resource);

  protected String toHubLabel(Resource hub) {
    return hub.getOutgoingEdges().stream()
      .filter(e -> CREATOR.getUri().equals(e.getPredicate().getUri()))
      .map(ResourceEdge::getTarget)
      .findFirst()
      .map(creator -> join(SPACE, creator.getLabel(), hub.getLabel()))
      .orElse(hub.getLabel());
  }
}
