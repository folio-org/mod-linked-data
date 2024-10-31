package org.folio.linked.data.service.resource;


import org.folio.linked.data.domain.dto.ResourceIdDto;
import org.folio.linked.data.domain.dto.ResourceMarcViewDto;
import org.folio.linked.data.domain.dto.ResourceResponseDto;

public interface ResourceMarcBibService {

  ResourceMarcViewDto getResourceMarcView(Long id);

  Boolean isSupportedByInventoryId(String inventoryId);

  ResourceResponseDto getResourcePreviewByInventoryId(String inventoryId);

  ResourceIdDto importMarcRecord(String inventoryId);
}
