package org.folio.linked.data.service.resource;

import org.folio.ld.dictionary.model.Resource;
import org.folio.linked.data.domain.dto.ResourceIdDto;
import org.folio.linked.data.domain.dto.ResourceMarcViewDto;
import org.folio.linked.data.domain.dto.ResourceResponseDto;

public interface ResourceMarcService {

  Long saveMarcResource(Resource modelResource);

  ResourceMarcViewDto getResourceMarcView(Long id);

  Boolean isSupportedByInventoryId(String inventoryId);

  ResourceResponseDto getResourcePreviewByInventoryId(String inventoryId);

  ResourceIdDto importMarcRecord(String inventoryId);

}
