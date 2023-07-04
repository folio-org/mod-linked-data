package org.folio.linked.data.mapper.resource.common;

import org.folio.linked.data.domain.dto.BibframeRequest;
import org.folio.linked.data.domain.dto.BibframeResponse;
import org.folio.linked.data.model.entity.Resource;

public interface BibframeProfiledMapper {

  Resource toResource(BibframeRequest dto);

  BibframeResponse toResponseDto(Resource resource);
}