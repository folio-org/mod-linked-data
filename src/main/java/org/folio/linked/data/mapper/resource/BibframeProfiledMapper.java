package org.folio.linked.data.mapper.resource;

import org.folio.linked.data.domain.dto.BibframeCreateRequest;
import org.folio.linked.data.domain.dto.BibframeResponse;
import org.folio.linked.data.model.entity.Resource;

public interface BibframeProfiledMapper {

  Resource toResource(BibframeCreateRequest dto);

  BibframeResponse toResponseDto(Resource resource);
}
