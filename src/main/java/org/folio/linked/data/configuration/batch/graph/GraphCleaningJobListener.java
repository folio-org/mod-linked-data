package org.folio.linked.data.configuration.batch.graph;

import static java.util.Optional.ofNullable;
import static org.folio.linked.data.configuration.batch.graph.GraphCleaningBatchJobConfig.JOB_PARAM_EXECUTION_ROUND;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.service.batch.BatchJobService;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameter;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.listener.JobExecutionListener;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.beans.factory.ObjectProvider;

@Log4j2
@RequiredArgsConstructor
public class GraphCleaningJobListener implements JobExecutionListener {

  private final ObjectProvider<BatchJobService> batchJobServiceProvider;
  private final int maxRounds;

  @Override
  public void afterJob(JobExecution jobExecution) {
    var totalWritten = jobExecution.getStepExecutions().stream()
      .mapToLong(StepExecution::getWriteCount)
      .sum();

    if (totalWritten > 0) {
      int nextRound = increaseExecutionRound(jobExecution.getJobParameters());
      if (nextRound > maxRounds) {
        log.info("GraphCleaningJob round exceeded maxRounds setting ({}), no further round to be executed", maxRounds);
      } else {
        log.info("GraphCleaningJob wrote {} item(s), launching next round [{}]", totalWritten, nextRound);
        batchJobServiceProvider.getIfAvailable().startGraphCleaning(nextRound);
      }
    } else {
      log.info("GraphCleaningJob wrote 0 items, no further run needed");
    }
  }

  private int increaseExecutionRound(JobParameters jobParameters) {
    return ofNullable(jobParameters.getParameter(JOB_PARAM_EXECUTION_ROUND))
      .map(JobParameter::value)
      .map(v -> (Integer) v)
      .map(r -> ++r)
      .orElse(2);
  }
}
