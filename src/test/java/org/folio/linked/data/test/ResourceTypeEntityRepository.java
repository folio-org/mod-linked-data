package org.folio.linked.data.test;

import java.util.Set;
import org.folio.linked.data.model.entity.ResourceTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ResourceTypeEntityRepository extends JpaRepository<ResourceTypeEntity, Long> {

  @Query(value = """
    SELECT tl.type_hash, tl.type_uri, tl.simple_label FROM type_lookup tl
    JOIN resource_type_map rtm ON rtm.type_hash = tl.type_hash
    JOIN resources r ON r.resource_hash = rtm.resource_hash
    WHERE r.resource_hash = :resourceId
    ORDER BY tl.type_hash""", nativeQuery = true)
  Set<ResourceTypeEntity> findByResourceId(@Param("resourceId") Long resourceId);
}
