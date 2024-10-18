package org.folio.linked.data.mapper.kafka.inventory;

import static org.folio.linked.data.util.Constants.SEARCH_WORK_RESOURCE_NAME;
import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import java.util.UUID;
import org.folio.linked.data.domain.dto.InventoryInstanceEvent;
import org.folio.linked.data.domain.dto.ResourceIndexEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = SPRING, imports = UUID.class)
public abstract class InventoryInstanceMapper {

  @Mapping(target = "id", expression = "java(UUID.randomUUID().toString())")
  @Mapping(target = "resourceName", constant = SEARCH_WORK_RESOURCE_NAME)
  public abstract ResourceIndexEvent toReindexEvent(InventoryInstanceEvent inventoryInstanceEvent);
}
