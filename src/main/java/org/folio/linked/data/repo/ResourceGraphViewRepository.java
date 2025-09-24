package org.folio.linked.data.repo;

import java.util.Set;
import org.folio.linked.data.model.entity.ResourceGraphView;
import org.springframework.data.repository.Repository;

public interface ResourceGraphViewRepository extends Repository<ResourceGraphView, Long> {

  Set<ResourceGraphView> findByInventoryIdIn(Set<String> inventoryIds);
}
