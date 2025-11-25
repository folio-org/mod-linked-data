package org.folio.linked.data.service.search.lccn;

import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.folio.ld.dictionary.model.Resource;
import org.folio.linked.data.model.entity.ResourceSubgraphView;

public interface LccnResourceService {

  Map<String, LccnResourceSearchResult> findMockResources(Set<Resource> resources);

  Resource unMockLccnResource(Resource resourceModel, Map<String, LccnResourceSearchResult> searchResults);

  record LccnResourceSearchResult(@Nullable ResourceSubgraphView subgraphView, @Nonnull String inventoryId) {
  }

}
