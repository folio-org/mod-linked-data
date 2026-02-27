package org.folio.linked.data.repo;

import org.folio.linked.data.model.entity.batch.BatchJobExecution;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BatchJobExecutionRepository extends JpaRepository<BatchJobExecution, Long> {
}

