package org.folio.linked.data.repo;

import java.util.Optional;
import org.folio.linked.data.model.entity.Predicate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PredicateRepository extends JpaRepository<Predicate, Long> {

  Optional<Predicate> findPredicateByLabel(String label);

}

