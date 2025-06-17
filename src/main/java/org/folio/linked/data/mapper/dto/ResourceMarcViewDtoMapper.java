package org.folio.linked.data.mapper.dto;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import org.folio.linked.data.domain.dto.ResourceMarcViewDto;
import org.folio.linked.data.model.entity.Resource;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = SPRING)
public interface ResourceMarcViewDtoMapper {

  @Mapping(target = "recordType", constant = "MARC_BIB")
  @Mapping(target = "parsedRecord.content", source = "marc")
  ResourceMarcViewDto toMarcViewDto(Resource resource, String marc);

}
