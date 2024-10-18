package org.folio.linked.data.test;

import java.util.Optional;
import java.util.Set;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.repo.ResourceRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
@Primary
public interface ResourceTestRepository extends ResourceRepository {

  @Query("SELECT r FROM Resource r "
    + "JOIN FETCH r.types t "
    + "LEFT JOIN FETCH r.incomingEdges "
    + "LEFT JOIN FETCH r.outgoingEdges "
    + "WHERE :typeCount = ("
    + "SELECT COUNT(DISTINCT t.uri) "
    + "FROM r.types t "
    + "WHERE t.uri IN :types"
    + ")")
  Page<Resource> findAllByTypeWithEdgesLoaded(@Param("types") Set<String> types, int typeCount, Pageable pageable);

  @Query("SELECT r FROM Resource r "
    + "LEFT JOIN FETCH r.incomingEdges "
    + "LEFT JOIN FETCH r.outgoingEdges "
    + "WHERE r.id = :id")
  Optional<Resource> findByIdWithEdgesLoaded(Long id);
}
