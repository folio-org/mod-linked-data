package org.folio.linked.data.service.reference;

import org.folio.linked.data.domain.dto.Reference;
import org.folio.linked.data.model.entity.Resource;

public interface ReferenceService {
  Resource resolveReference(Reference reference);
}
