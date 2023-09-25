package org.folio.linked.data.mapper.resource.common.inner;

import org.folio.linked.data.domain.dto.ResourceDto;
import org.folio.linked.data.model.entity.Resource;

public interface InnerResourceMapperUnit {

  ResourceDto toDto(Resource source, ResourceDto destination);

  Resource toEntity(Object innerResourceDto);
}
