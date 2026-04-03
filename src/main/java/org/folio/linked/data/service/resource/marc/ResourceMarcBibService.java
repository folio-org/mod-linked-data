package org.folio.linked.data.service.resource.marc;

import org.folio.ld.dictionary.model.Resource;
import org.folio.linked.data.domain.dto.ResourceIdDto;
import org.folio.linked.data.domain.dto.ResourceMarcViewDto;
import org.folio.linked.data.domain.dto.ResourceResponseDto;

public interface ResourceMarcBibService {

  ResourceMarcViewDto getResourceMarcView(Long id);

  boolean checkMarcBibImportableToGraph(String inventoryId);

  ResourceResponseDto getResourcePreviewByInventoryId(String inventoryId);

  ResourceIdDto importMarcRecord(String inventoryId, Integer profileId);

  boolean saveAdminMetadata(Resource modelResource);

}
