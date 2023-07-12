package org.folio.linked.data.mapper.resource.common;

import org.folio.linked.data.domain.dto.BibframeRequest;
import org.folio.linked.data.domain.dto.BibframeResponse;
import org.folio.linked.data.model.entity.Resource;

public interface ProfiledMapperUnit {

  Resource toEntity(BibframeRequest dto);

  BibframeResponse toDto(Resource resource);
}
