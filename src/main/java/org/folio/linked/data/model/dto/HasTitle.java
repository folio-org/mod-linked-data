package org.folio.linked.data.model.dto;

import org.folio.linked.data.domain.dto.TitleFieldResponseTitleInner;

public interface HasTitle {
  HasTitle addTitleItem(TitleFieldResponseTitleInner titleItem);
}
