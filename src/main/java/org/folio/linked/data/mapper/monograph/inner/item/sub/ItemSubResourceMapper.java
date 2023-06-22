package org.folio.linked.data.mapper.monograph.inner.item.sub;

import org.folio.linked.data.domain.dto.Item;
import org.folio.linked.data.mapper.resource.inner.sub.SubResourceMapper;

public interface ItemSubResourceMapper extends SubResourceMapper<Item> {

  @Override
  default Class<Item> destinationDtoClass() {
    return Item.class;
  }
}
