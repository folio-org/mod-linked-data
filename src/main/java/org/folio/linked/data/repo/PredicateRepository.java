package org.folio.linked.data.repo;

import static org.folio.linked.data.util.Constants.CONDITION_TO_CACHE;

import java.util.Optional;
import org.folio.linked.data.model.entity.Predicate;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PredicateRepository extends JpaRepository<Predicate, Long> {

  @Cacheable(value = "predicates", condition = CONDITION_TO_CACHE)
  Optional<Predicate> findPredicateByLabel(String label);

}

