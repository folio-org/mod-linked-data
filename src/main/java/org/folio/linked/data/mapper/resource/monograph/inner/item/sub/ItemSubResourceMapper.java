package org.folio.linked.data.mapper.resource.monograph.inner.item.sub;

import org.folio.linked.data.domain.dto.Item;
import org.folio.linked.data.mapper.resource.common.inner.sub.SubResourceMapperUnit;

public interface ItemSubResourceMapper extends SubResourceMapperUnit<Item> {

  @Override
  default Class<Item> getParentDto() {
    return Item.class;
  }
}
