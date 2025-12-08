package org.folio.linked.data.service.lccn;

import static org.folio.linked.data.util.Constants.STANDALONE_PROFILE;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.folio.ld.dictionary.model.Resource;
import org.folio.rdf4ld.service.lccn.MockLccnResourceService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Profile(STANDALONE_PROFILE)
public class LccnResourceServiceStandalone implements LccnResourceService {
  private final MockLccnResourceService mockLccnResourceService;

  @Override
  public Map<String, LccnResourceSearchResult> findMockResources(Set<Resource> resources) {
    return Map.of();
  }

  @Override
  public Resource unMockLccnEdges(Resource resource, Map<String, LccnResourceSearchResult> searchResults) {
    return mockLccnResourceService.unMockLccnEdges(resource, lccn -> Optional.empty());
  }
}
