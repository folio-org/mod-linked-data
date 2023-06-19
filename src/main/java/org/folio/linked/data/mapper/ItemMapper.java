package org.folio.linked.data.mapper;

import org.folio.linked.data.domain.dto.Item;
import org.folio.linked.data.model.entity.Resource;

public interface ItemMapper {

  Item toItem(Resource resource);

}
