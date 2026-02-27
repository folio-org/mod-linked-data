package org.folio.linked.data.model.entity.batch;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(of = "stepExecutionId")
@Entity
public class BatchStepExecution {

  @Id
  private Long stepExecutionId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "job_execution_id")
  private BatchJobExecution jobExecution;

  private String stepName;

  private Long readCount;

  private Long writeCount;
}
