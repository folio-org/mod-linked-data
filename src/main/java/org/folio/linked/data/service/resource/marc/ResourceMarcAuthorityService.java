package org.folio.linked.data.service.resource.marc;

import org.folio.ld.dictionary.model.Resource;
import org.folio.linked.data.model.dto.Identifiable;

public interface ResourceMarcAuthorityService {

  Long saveMarcAuthority(Resource modelResource);

  org.folio.linked.data.model.entity.Resource fetchAuthorityOrCreateFromSrsRecord(Identifiable identifiable);

  boolean isMarcAuthorityCompatibleWithTarget(String marc, AssignAuthorityTarget target);
}
