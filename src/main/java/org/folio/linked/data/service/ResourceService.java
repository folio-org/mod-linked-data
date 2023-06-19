package org.folio.linked.data.service;

import org.folio.linked.data.domain.dto.ResourceResponse;
import org.folio.linked.data.domain.dto.ResourceShortInfoPage;

public interface ResourceService {


  ResourceResponse getResourceById(Long id);


  ResourceShortInfoPage getResourceShortInfoPage(Integer pageNumber, Integer pageSize);
}
