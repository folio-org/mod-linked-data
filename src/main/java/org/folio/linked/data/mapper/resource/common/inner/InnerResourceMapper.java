package org.folio.linked.data.mapper.resource.common.inner;

import java.util.Optional;
import lombok.NonNull;
import org.folio.linked.data.domain.dto.ResourceDto;
import org.folio.linked.data.model.entity.Resource;

public interface InnerResourceMapper {

  ResourceDto toDto(@NonNull Resource source, @NonNull ResourceDto destination);

  Resource toEntity(@NonNull Object dto, @NonNull String resourceType);

  Optional<InnerResourceMapperUnit> getMapperUnit(String type);

}
