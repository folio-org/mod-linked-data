package org.folio.linked.data.repo;

import java.util.Set;
import org.folio.linked.data.model.entity.ResourceSubgraphView;
import org.springframework.data.repository.CrudRepository;

public interface ResourceSubgraphViewRepository extends CrudRepository<ResourceSubgraphView, Long> {

  Set<ResourceSubgraphView> findByInventoryIdIn(Set<String> inventoryIds);
}
