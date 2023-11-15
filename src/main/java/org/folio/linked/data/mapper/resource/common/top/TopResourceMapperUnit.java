package org.folio.linked.data.mapper.resource.common.top;

import org.folio.linked.data.domain.dto.ResourceDto;
import org.folio.linked.data.model.entity.Resource;

public interface TopResourceMapperUnit {

  ResourceDto toDto(Resource source, ResourceDto destination);

  Resource toEntity(Object topResourceDto);
}
