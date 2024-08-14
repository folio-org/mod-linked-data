package org.folio.linked.data.service.resource;

import java.util.Set;
import org.folio.linked.data.domain.dto.ResourceIdDto;
import org.folio.linked.data.domain.dto.ResourceRequestDto;
import org.folio.linked.data.domain.dto.ResourceResponseDto;
import org.folio.linked.data.domain.dto.ResourceShortInfoPage;

public interface ResourceService {

  ResourceResponseDto createResource(ResourceRequestDto resourceRequest);

  ResourceResponseDto getResourceById(Long id);

  ResourceIdDto getResourceIdByInventoryId(String inventoryId);

  ResourceResponseDto updateResource(Long id, ResourceRequestDto bibframeRequest);

  void deleteResource(Long id);

  ResourceShortInfoPage getResourceShortInfoPage(String type, Integer pageNumber, Integer pageSize);

  void updateIndexDateBatch(Set<Long> ids);

}
