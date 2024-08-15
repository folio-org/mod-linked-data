package org.folio.linked.data.mapper.dto;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import org.folio.linked.data.model.entity.FolioMetadata;
import org.mapstruct.Mapper;

@Mapper(componentModel = SPRING)
public interface FolioMetadataMapper {

  org.folio.linked.data.domain.dto.FolioMetadata toDto(FolioMetadata entity);

}
