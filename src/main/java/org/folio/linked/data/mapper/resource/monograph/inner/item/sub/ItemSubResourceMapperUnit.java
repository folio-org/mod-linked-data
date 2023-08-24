package org.folio.linked.data.mapper.resource.monograph.inner.item.sub;

import java.util.Set;
import org.folio.linked.data.domain.dto.Item2;
import org.folio.linked.data.mapper.resource.common.inner.sub.SubResourceMapper;
import org.folio.linked.data.mapper.resource.common.inner.sub.SubResourceMapperUnit;
import org.folio.linked.data.model.entity.Resource;

public interface ItemSubResourceMapperUnit extends SubResourceMapperUnit<Item2> {

  @Override
  default Set<Class> getParentDto() {
    return Set.of(Item2.class);
  }

  // required until we implement it for every Item mapper. Should be removed after.
  default Resource toEntity(Object dto, String predicate, SubResourceMapper subResourceMapper) {
    return null;
  }

}
