package org.folio.linked.data.mapper.dto.resource;

import org.folio.linked.data.domain.dto.ResourceRequestDto;
import org.folio.linked.data.domain.dto.ResourceResponseDto;
import org.folio.linked.data.model.entity.Resource;

public interface ResourceDtoMapper {
  Resource toEntity(ResourceRequestDto dto);

  ResourceResponseDto toDto(Resource resource);
}
