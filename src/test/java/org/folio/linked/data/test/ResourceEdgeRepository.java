package org.folio.linked.data.test;

import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.model.entity.pk.ResourceEdgePk;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResourceEdgeRepository extends JpaRepository<ResourceEdge, ResourceEdgePk> {

}
