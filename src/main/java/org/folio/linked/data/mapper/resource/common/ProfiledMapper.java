package org.folio.linked.data.mapper.resource.common;

import org.folio.linked.data.domain.dto.BibframeRequest;
import org.folio.linked.data.domain.dto.BibframeResponse;
import org.folio.linked.data.model.entity.Resource;

public interface ProfiledMapper {

  BibframeResponse toDto(Resource resource);

  Resource toEntity(BibframeRequest dto);

}
