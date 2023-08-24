package org.folio.linked.data.mapper.resource.common.inner;

import org.folio.linked.data.domain.dto.Bibframe2Response;
import org.folio.linked.data.model.entity.Resource;

public interface InnerResourceMapperUnit {

  Bibframe2Response toDto(Resource source, Bibframe2Response destination);

  Resource toEntity(Object innerResourceDto);
}
