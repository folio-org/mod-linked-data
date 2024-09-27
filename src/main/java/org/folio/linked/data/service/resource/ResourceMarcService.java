package org.folio.linked.data.service.resource;

import org.folio.ld.dictionary.model.Resource;
import org.folio.linked.data.domain.dto.ResourceMarcViewDto;

public interface ResourceMarcService {

  Long saveMarcResource(Resource modelResource);

  ResourceMarcViewDto getResourceMarcView(Long id);

}
