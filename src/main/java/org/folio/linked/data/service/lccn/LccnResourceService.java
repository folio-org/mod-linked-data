package org.folio.linked.data.service.lccn;

import java.util.Map;
import java.util.Set;
import org.folio.ld.dictionary.model.Resource;
import org.folio.linked.data.model.entity.ResourceSubgraphView;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public interface LccnResourceService {

  Map<String, LccnResourceSearchResult> findMockResources(Set<Resource> resources);

  Resource unMockLccnEdges(Resource resourceModel, Map<String, LccnResourceSearchResult> searchResults);

  record LccnResourceSearchResult(@Nullable ResourceSubgraphView subgraphView, @NonNull String inventoryId) {
  }

}
