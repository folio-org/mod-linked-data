package org.folio.linked.data.mapper.resource.common.inner;

import lombok.NonNull;
import org.folio.linked.data.domain.dto.BibframeResponse;
import org.folio.linked.data.model.entity.Resource;

public interface InnerResourceMapper {

  BibframeResponse toDto(@NonNull Resource source, @NonNull BibframeResponse destination);

  Resource toEntity(@NonNull Object dto, @NonNull String resourceType);
}
