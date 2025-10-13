package org.folio.linked.data.mapper.kafka.search;

import static org.folio.linked.data.util.Constants.SEARCH_HUB_RESOURCE_NAME;
import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import java.util.UUID;
import org.folio.linked.data.domain.dto.LinkedDataHub;
import org.folio.linked.data.domain.dto.ResourceIndexEvent;
import org.folio.linked.data.model.entity.Resource;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = SPRING, imports = UUID.class)
public interface HubSearchMessageMapper {

  @Mapping(target = "id", expression = "java(UUID.randomUUID().toString())")
  @Mapping(target = "resourceName", constant = SEARCH_HUB_RESOURCE_NAME)
  @Mapping(target = "_new", expression = "java(toLinkedDataHub(resource))")
  ResourceIndexEvent toIndex(Resource resource);

  @Mapping(source = "id", target = "id")
  @Mapping(source = "label", target = "label")
  LinkedDataHub toLinkedDataHub(Resource resource);
}
