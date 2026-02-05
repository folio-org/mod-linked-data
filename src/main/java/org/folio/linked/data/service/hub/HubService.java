package org.folio.linked.data.service.hub;

import org.folio.linked.data.domain.dto.ResourceResponseDto;

public interface HubService {

  ResourceResponseDto previewHub(String hubUri);

  ResourceResponseDto saveHub(String hubUri);

}
