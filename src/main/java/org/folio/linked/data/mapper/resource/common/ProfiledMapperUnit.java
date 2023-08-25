package org.folio.linked.data.mapper.resource.common;

import org.folio.linked.data.domain.dto.Bibframe2Request;
import org.folio.linked.data.domain.dto.Bibframe2Response;
import org.folio.linked.data.model.entity.Resource;

public interface ProfiledMapperUnit {

  Resource toEntity(Bibframe2Request dto);

  Bibframe2Response toDto(Resource resource);
}
