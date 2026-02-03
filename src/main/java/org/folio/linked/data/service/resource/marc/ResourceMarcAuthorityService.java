package org.folio.linked.data.service.resource.marc;

import java.util.Optional;
import org.folio.linked.data.domain.dto.AssignmentCheckResponseDto;
import org.folio.linked.data.model.entity.Resource;

public interface ResourceMarcAuthorityService {

  Long saveMarcAuthority(org.folio.ld.dictionary.model.Resource modelResource);

  Resource importResourceFromSrs(String srsId);

  Optional<org.folio.ld.dictionary.model.Resource> fetchResourceFromSrsByInventoryId(String inventoryId);

  AssignmentCheckResponseDto validateAuthorityAssignment(String marc, AssignAuthorityTarget target);
}
