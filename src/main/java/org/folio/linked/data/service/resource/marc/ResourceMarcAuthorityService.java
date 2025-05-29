package org.folio.linked.data.service.resource.marc;

import java.util.Optional;
import org.folio.linked.data.domain.dto.AssignmentCheckResponseDto;
import org.folio.linked.data.model.dto.Identifiable;
import org.folio.linked.data.model.entity.Resource;

public interface ResourceMarcAuthorityService {

  Long saveMarcAuthority(org.folio.ld.dictionary.model.Resource modelResource);

  Resource fetchAuthorityOrCreateFromSrsRecord(Identifiable identifiable);

  Optional<Resource> fetchAuthorityOrCreateByInventoryId(String  inventoryId);

  AssignmentCheckResponseDto validateAuthorityAssignment(String marc, AssignAuthorityTarget target);
}
