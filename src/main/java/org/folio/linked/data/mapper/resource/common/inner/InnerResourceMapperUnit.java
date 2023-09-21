package org.folio.linked.data.mapper.resource.common.inner;

import org.apache.commons.lang3.NotImplementedException;
import org.folio.linked.data.domain.dto.Bibframe2Response;
import org.folio.linked.data.domain.dto.ResourceDto;
import org.folio.linked.data.model.entity.Resource;

public interface InnerResourceMapperUnit {

  ResourceDto toDto(Resource source, ResourceDto destination);

  default Bibframe2Response toDto(Resource source, Bibframe2Response destination) {
    throw new NotImplementedException();
  }

  Resource toEntity(Object innerResourceDto);
}
