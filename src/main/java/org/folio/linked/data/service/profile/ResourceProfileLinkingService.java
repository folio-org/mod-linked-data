package org.folio.linked.data.service.profile;

import java.util.Optional;
import org.folio.linked.data.model.entity.Resource;

public interface ResourceProfileLinkingService {
  void linkResourceToProfile(Resource resource, Integer profileId);

  Optional<Integer> getLinkedProfile(Resource resource);
}
