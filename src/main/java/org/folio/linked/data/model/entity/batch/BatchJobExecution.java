package org.folio.linked.data.model.entity.batch;

import static org.folio.linked.data.configuration.batch.graph.GraphCleaningBatchJobConfig.JOB_PARAM_EXECUTION_ROUND;
import static org.folio.linked.data.configuration.batch.reindex.ReindexBatchJobConfig.JOB_PARAM_IS_FULL_REINDEX;
import static org.folio.linked.data.configuration.batch.reindex.ReindexBatchJobConfig.JOB_PARAM_STARTED_BY;

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

  private static final String QUERY_PARAM = "(SELECT p.parameter_value FROM batch_job_execution_params p"
    + " WHERE p.job_execution_id = job_execution_id AND p.parameter_name = '";
  private static final String QUERY_PARAM_END = "')";
  @Id
  private Long jobExecutionId;

  private LocalDateTime startTime;

  private LocalDateTime endTime;

  private String status;

  @Formula(QUERY_PARAM + JOB_PARAM_STARTED_BY + QUERY_PARAM_END)
  private String startedBy;

  @Formula(QUERY_PARAM + JOB_PARAM_IS_FULL_REINDEX + QUERY_PARAM_END)
  private String isFullReindex;

  @Formula(QUERY_PARAM + JOB_PARAM_EXECUTION_ROUND + QUERY_PARAM_END)
  private Integer executionRound;

  @OneToMany(mappedBy = "jobExecution", fetch = FetchType.EAGER)
  private Set<BatchStepExecution> stepExecutions;
}
