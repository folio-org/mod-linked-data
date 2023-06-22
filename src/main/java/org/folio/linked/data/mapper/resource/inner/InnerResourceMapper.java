package org.folio.linked.data.mapper.resource.inner;

import org.folio.linked.data.domain.dto.BibframeResponse;
import org.folio.linked.data.model.entity.Resource;

public interface InnerResourceMapper<T> {

  BibframeResponse toDto(Resource source, BibframeResponse destination);

  Resource toResource(T dto);
}
