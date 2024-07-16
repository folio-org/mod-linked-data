package org.folio.linked.data.mapper.kafka.inventory;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import org.folio.linked.data.mapper.ResourceModelMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.marc4ld.service.ld2marc.Bibframe2MarcMapper;
import org.folio.search.domain.dto.InstanceIngressEvent;
import org.folio.search.domain.dto.InstanceIngressPayload;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = SPRING)
public abstract class KafkaInventoryMessageMapper {

  private static final String LINKED_DATA_ID = "linkedDataId";
  @Autowired
  protected Bibframe2MarcMapper bibframe2MarcMapper;
  @Autowired
  protected ResourceModelMapper resourceModelMapper;

  @Mapping(target = "eventPayload", source = "resource")
  public abstract InstanceIngressEvent toInstanceIngressEvent(Resource resource);

  @Mapping(target = "sourceRecordIdentifier", source = "instanceMetadata.inventoryId")
  @Mapping(target = "sourceType", constant = "LINKED_DATA")
  @Mapping(target = "sourceRecordObject", source = "resource")
  protected abstract InstanceIngressPayload toInstanceIngressPayload(Resource resource);

  protected String toMarcJson(Resource resource) {
    var resourceModel = resourceModelMapper.toModel(resource);
    return bibframe2MarcMapper.toMarcJson(resourceModel);
  }

  @AfterMapping
  protected void afterMappingPayload(@MappingTarget InstanceIngressPayload payload, Resource resource) {
    payload.putAdditionalProperty(LINKED_DATA_ID, resource.getId());
  }
}
