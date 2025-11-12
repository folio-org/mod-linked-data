package org.folio.linked.data.repo;

import org.folio.linked.data.model.entity.imprt.ImportEventResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImportEventResultRepository extends JpaRepository<ImportEventResult, Long> {
}
