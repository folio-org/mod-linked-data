package org.folio.linked.data.service;

import java.util.Set;
import org.folio.linked.data.domain.dto.ResourceDto;
import org.folio.linked.data.domain.dto.ResourceShortInfoPage;

public interface ResourceService {

  ResourceDto createResource(ResourceDto resourceRequest);

  Long createResource(org.folio.marc4ld.model.Resource resource);

  ResourceDto getResourceById(Long id);

  ResourceDto updateResource(Long id, ResourceDto bibframeRequest);

  void deleteResource(Long id);

  ResourceShortInfoPage getResourceShortInfoPage(String type, Integer pageNumber, Integer pageSize);

  void updateIndexDateBatch(Set<Long> ids);
}
