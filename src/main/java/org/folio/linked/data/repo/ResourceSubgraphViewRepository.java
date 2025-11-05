package org.folio.linked.data.repo;

import java.util.Set;
import org.folio.linked.data.model.entity.ResourceSubgraphView;
import org.springframework.data.repository.Repository;

public interface ResourceSubgraphViewRepository extends Repository<ResourceSubgraphView, Long> {

  Set<ResourceSubgraphView> findByInventoryIdIn(Set<String> inventoryIds);
}
