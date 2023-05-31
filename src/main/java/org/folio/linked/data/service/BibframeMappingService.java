package org.folio.linked.data.service;

import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.domain.dto.Item;
import org.folio.linked.data.domain.dto.Work;
import org.folio.linked.data.model.entity.Resource;

public interface BibframeMappingService {

  Work toWork(Resource resource);

  Instance toInstance(Resource resource);

  Item toItem(Resource resource);

}
