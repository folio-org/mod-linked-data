package org.folio.linked.data.repo;

import java.util.Optional;
import java.util.Set;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ResourceRepository extends JpaRepository<Resource, Long> {

  @Modifying
  @Query("update Resource r set r.indexDate = current_timestamp() where r.id = :id")
  void updateIndexDate(@Param("id") Long id);

  @Modifying
  @Query("update Resource r set r.indexDate = current_timestamp() where r.id in (:ids)")
  void updateIndexDateBatch(@Param("ids") Set<Long> ids);

  Optional<Resource> findByFolioMetadataSrsId(String srsId);
}
