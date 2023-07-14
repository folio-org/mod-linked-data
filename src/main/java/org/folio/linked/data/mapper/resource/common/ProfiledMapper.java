package org.folio.linked.data.mapper.resource.common;

import lombok.NonNull;
import org.folio.linked.data.domain.dto.BibframeRequest;
import org.folio.linked.data.domain.dto.BibframeResponse;
import org.folio.linked.data.model.entity.Resource;

public interface ProfiledMapper {

  BibframeResponse toDto(@NonNull Resource resource);

  Resource toEntity(@NonNull BibframeRequest dto);

}
