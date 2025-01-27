package org.folio.linked.data.mapper.dto.monograph;

import java.util.Set;
import org.folio.linked.data.domain.dto.ResourceRequestDto;
import org.folio.linked.data.domain.dto.ResourceResponseDto;
import org.folio.linked.data.mapper.dto.common.SingleResourceMapperUnit;

public abstract class TopResourceMapperUnit implements SingleResourceMapperUnit {

  private static final Set<Class<?>> SUPPORTED_PARENTS = Set.of(
    ResourceRequestDto.class,
    ResourceResponseDto.class
  );

  @Override
  public Set<Class<?>> supportedParents() {
    return SUPPORTED_PARENTS;
  }

}
