package org.folio.linked.data.test;

import java.util.Set;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ResourceTestRepository extends JpaRepository<Resource, Long> {

  @Query("SELECT r FROM Resource r "
    + "JOIN FETCH r.types t "
    + "LEFT JOIN FETCH r.incomingEdges "
    + "LEFT JOIN FETCH r.outgoingEdges "
    + "WHERE t.uri IN :types")
  Page<Resource> findAllByTypeWithEdgesLoaded(@Param("types") Set<String> types, Pageable pageable);
}
