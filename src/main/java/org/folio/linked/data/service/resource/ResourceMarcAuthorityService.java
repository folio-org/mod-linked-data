package org.folio.linked.data.service.resource;

import org.folio.ld.dictionary.model.Resource;
import org.folio.linked.data.model.dto.Identifiable;

public interface ResourceMarcAuthorityService {

  Long saveMarcResource(Resource modelResource);

  org.folio.linked.data.model.entity.Resource fetchResourceOrCreateFromSrsRecord(Identifiable identifiable);
}
