package org.folio.linked.data.mapper.dto;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import org.folio.linked.data.model.entity.InstanceMetadata;
import org.mapstruct.Mapper;

@Mapper(componentModel = SPRING)
public interface InstanceMetadataMapper {

  org.folio.linked.data.domain.dto.InstanceMetadata toDto(InstanceMetadata entity);

}
