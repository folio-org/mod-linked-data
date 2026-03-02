package org.folio.linked.data.model.entity.batch;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Formula;

@Getter
@Setter
@EqualsAndHashCode(of = "jobExecutionId")
@Entity
public class BatchJobExecution {

  @Id
  private Long jobExecutionId;

  private LocalDateTime startTime;

  private LocalDateTime endTime;

  private String status;

  @Formula("(SELECT p.parameter_value FROM batch_job_execution_params p"
    + " WHERE p.job_execution_id = job_execution_id AND p.parameter_name = 'startedBy')")
  private String startedBy;

  @Formula("(SELECT p.parameter_value FROM batch_job_execution_params p"
    + " WHERE p.job_execution_id = job_execution_id AND p.parameter_name = 'isFullReindex')")
  private String isFullReindex;

  @OneToMany(mappedBy = "jobExecution", fetch = FetchType.EAGER)
  private Set<BatchStepExecution> stepExecutions;
}
