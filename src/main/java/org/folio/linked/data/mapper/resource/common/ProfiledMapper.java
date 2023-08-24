package org.folio.linked.data.mapper.resource.common;

import lombok.NonNull;
import org.folio.linked.data.domain.dto.Bibframe2Request;
import org.folio.linked.data.domain.dto.Bibframe2Response;
import org.folio.linked.data.model.entity.Resource;

public interface ProfiledMapper {

  Bibframe2Response toDto(@NonNull Resource resource);

  Resource toEntity(@NonNull Bibframe2Request dto);

}
