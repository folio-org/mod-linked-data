package org.folio.linked.data.repo;

import java.util.List;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.model.entity.pk.ResourceEdgePk;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResourceEdgeRepository extends JpaRepository<ResourceEdge, ResourceEdgePk> {

  List<ResourceEdge> findByIdSourceHash(Long hash);

  List<ResourceEdge> findByIdTargetHash(Long hash);

  Long deleteByIdSourceHashAndIdPredicateHash(Long sourceHash, Long predicateHash);
}
