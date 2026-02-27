package org.folio.linked.data.model.entity.batch;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(of = {"jobExecutionId", "parameterName"})
@Entity
@IdClass(BatchJobExecutionParams.BatchJobExecutionParamsId.class)
public class BatchJobExecutionParams {

  @Id
  private Long jobExecutionId;

  @Id
  private String parameterName;

  private String parameterValue;

  @Data
  public static class BatchJobExecutionParamsId implements Serializable {
    private Long jobExecutionId;
    private String parameterName;
  }

}
