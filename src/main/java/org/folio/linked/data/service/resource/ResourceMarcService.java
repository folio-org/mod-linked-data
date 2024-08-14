package org.folio.linked.data.service.resource;

import org.folio.linked.data.domain.dto.ResourceMarcViewDto;

public interface ResourceMarcService {

  Long saveMarcResource(org.folio.ld.dictionary.model.Resource modelResource);

  ResourceMarcViewDto getResourceMarcView(Long id);

}
