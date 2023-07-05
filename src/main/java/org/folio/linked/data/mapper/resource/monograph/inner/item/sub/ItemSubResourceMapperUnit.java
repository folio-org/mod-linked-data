package org.folio.linked.data.mapper.resource.monograph.inner.item.sub;

import org.folio.linked.data.domain.dto.Item;
import org.folio.linked.data.mapper.resource.common.inner.sub.SubResourceMapperUnit;
import org.folio.linked.data.model.entity.Resource;

public interface ItemSubResourceMapperUnit extends SubResourceMapperUnit<Item> {

  @Override
  default Class<Item> getParentDto() {
    return Item.class;
  }

  // required until we implement it for every Item mapper. Should be removed after.
  default Resource toEntity(Object dto, String predicate) {
    return null;
  }

}
