package org.folio.linked.data.repo;

import java.util.List;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.model.entity.pk.ResourceEdgePk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ResourceEdgeRepository extends JpaRepository<ResourceEdge, ResourceEdgePk> {

  List<ResourceEdge> findByIdSourceHash(Long hash);

  List<ResourceEdge> findByIdTargetHash(Long hash);

  @Query("""
           SELECT r.id.targetHash
             FROM ResourceEdge r
            WHERE r.id.sourceHash = :sourceHash
              AND r.id.predicateHash = :predicateHash""")
  List<Long> findTargetHashes(@Param("sourceHash") Long sourceHash, @Param("predicateHash") Long predicateHash);

}
