package org.folio.linked.data.repo;

import java.util.Set;
import org.folio.linked.data.model.ResourceShortInfo;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ResourceRepository extends JpaRepository<Resource, Long> {


  @Query("SELECT r FROM Resource r JOIN r.type t WHERE t.typeUri IN :types OR t.simpleLabel IN :types")
  Page<ResourceShortInfo> findResourcesByType(@Param("types") Set<String> types, Pageable pageable);

}
