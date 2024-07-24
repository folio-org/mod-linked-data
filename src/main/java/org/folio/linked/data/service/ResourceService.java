package org.folio.linked.data.service;

import java.util.Set;
import org.folio.linked.data.domain.dto.ResourceGraphDto;
import org.folio.linked.data.domain.dto.ResourceIdDto;
import org.folio.linked.data.domain.dto.ResourceMarcViewDto;
import org.folio.linked.data.domain.dto.ResourceRequestDto;
import org.folio.linked.data.domain.dto.ResourceResponseDto;
import org.folio.linked.data.domain.dto.ResourceShortInfoPage;
import org.folio.linked.data.model.entity.Resource;

public interface ResourceService {

  ResourceResponseDto createResource(ResourceRequestDto resourceRequest);

  Long createResource(org.folio.ld.dictionary.model.Resource resource);

  ResourceResponseDto getResourceById(Long id);

  ResourceIdDto getResourceIdByInventoryId(String inventoryId);

  ResourceResponseDto updateResource(Long id, ResourceRequestDto bibframeRequest);

  void deleteResource(Long id);

  ResourceMarcViewDto getResourceMarcViewById(Long id);

  ResourceShortInfoPage getResourceShortInfoPage(String type, Integer pageNumber, Integer pageSize);

  void updateIndexDateBatch(Set<Long> ids);

  ResourceGraphDto getResourceGraphById(Long id);

  Resource saveMergingGraph(Resource resource);
}
