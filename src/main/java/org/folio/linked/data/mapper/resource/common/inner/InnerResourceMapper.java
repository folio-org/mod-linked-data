package org.folio.linked.data.mapper.resource.common.inner;

import lombok.NonNull;
import org.folio.linked.data.domain.dto.ResourceDto;
import org.folio.linked.data.model.entity.Resource;

public interface InnerResourceMapper {

  ResourceDto toDto(@NonNull Resource source, @NonNull ResourceDto destination);

  Resource toEntity(@NonNull Object dto, @NonNull String resourceType);
}
