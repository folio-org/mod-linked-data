package org.folio.linked.data.repo;

import java.util.List;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.model.entity.pk.ResourceEdgePk;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResourceEdgeRepository extends JpaRepository<ResourceEdge, ResourceEdgePk> {

  List<SourceOnly> findByIdTargetHash(Long hash);

  interface SourceOnly {
    Resource getSource();
  }
}
