package org.folio.linked.data.service.resource;

import java.util.Set;
import org.folio.ld.dictionary.model.Resource;
import org.folio.linked.data.domain.dto.ResourceIdDto;
import org.folio.linked.data.domain.dto.ResourceRequestDto;
import org.folio.linked.data.domain.dto.ResourceResponseDto;
import org.folio.linked.data.domain.dto.SearchResourcesRequestDto;

public interface ResourceService {

  ResourceResponseDto createResource(ResourceRequestDto resourceRequest);

  ResourceResponseDto getResourceById(Long id);

  ResourceIdDto getResourceIdByInventoryId(String inventoryId);

  Set<Resource> searchResources(SearchResourcesRequestDto searchRequest);

  ResourceResponseDto updateResource(Long id, ResourceRequestDto bibframeRequest);

  void deleteResource(Long id);

  void updateIndexDateBatch(Set<Long> ids);

}
