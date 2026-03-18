package org.folio.linked.data.configuration.batch.graph;

import static org.folio.linked.data.configuration.batch.graph.GraphCleaningBatchJobConfig.JOB_PARAM_EXECUTION_ROUND;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.folio.linked.data.service.batch.BatchJobService;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameter;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.beans.factory.ObjectProvider;

@UnitTest
@ExtendWith(MockitoExtension.class)
class GraphCleaningJobListenerTest {

  @InjectMocks
  private GraphCleaningJobListener listener;
  @Mock
  private ObjectProvider<BatchJobService> batchJobServiceProvider;

  @Test
  void afterJob_shouldStartNextRound_whenItemsWereWritten() {
    // given
    var batchJobService = mock(BatchJobService.class);
    when(batchJobServiceProvider.getIfAvailable()).thenReturn(batchJobService);
    var jobExecution = jobExecutionWithWriteCount(5L, executionRoundParams(1));

    // when
    listener.afterJob(jobExecution);

    // then
    verify(batchJobService).startGraphCleaning(2);
  }

  @Test
  void afterJob_shouldStartNextRoundFrom2_whenExecutionRoundParamAbsent() {
    // given
    var batchJobService = mock(BatchJobService.class);
    when(batchJobServiceProvider.getIfAvailable()).thenReturn(batchJobService);
    var jobExecution = jobExecutionWithWriteCount(3L, new JobParameters(Set.of()));

    // when
    listener.afterJob(jobExecution);

    // then
    verify(batchJobService).startGraphCleaning(2);
  }

  @Test
  void afterJob_shouldSumWriteCountsAcrossAllSteps() {
    // given
    var batchJobService = mock(BatchJobService.class);
    when(batchJobServiceProvider.getIfAvailable()).thenReturn(batchJobService);
    var jobExecution = jobExecutionWithMultipleSteps(List.of(2L, 3L), executionRoundParams(0));

    // when
    listener.afterJob(jobExecution);

    // then
    verify(batchJobService).startGraphCleaning(1);
  }

  @Test
  void afterJob_shouldNotStartNextRound_whenNoItemsWereWritten() {
    // given
    var jobExecution = mock(JobExecution.class);
    var step = mock(StepExecution.class);
    when(step.getWriteCount()).thenReturn(0L);
    when(jobExecution.getStepExecutions()).thenReturn(Set.of(step));
    var batchJobService = mock(BatchJobService.class);

    // when
    listener.afterJob(jobExecution);

    // then
    verify(batchJobService, never()).startGraphCleaning(anyInt());
    verify(batchJobServiceProvider, never()).getIfAvailable();
  }

  @Test
  void afterJob_shouldNotStartNextRound_whenStepExecutionsAreEmpty() {
    // given
    var batchJobService = mock(BatchJobService.class);
    var jobExecution = mock(JobExecution.class);
    when(jobExecution.getStepExecutions()).thenReturn(Set.of());

    // when
    listener.afterJob(jobExecution);

    // then
    verify(batchJobService, never()).startGraphCleaning(anyInt());
    verify(batchJobServiceProvider, never()).getIfAvailable();
  }

  private JobExecution jobExecutionWithWriteCount(long writeCount, JobParameters params) {
    return jobExecutionWithMultipleSteps(List.of(writeCount), params);
  }

  private JobExecution jobExecutionWithMultipleSteps(List<Long> writeCounts, JobParameters params) {
    var jobExecution = mock(JobExecution.class);
    when(jobExecution.getJobParameters()).thenReturn(params);
    Set<StepExecution> steps = new HashSet<>();
    for (var writeCount : writeCounts) {
      var step = mock(StepExecution.class);
      when(step.getWriteCount()).thenReturn(writeCount);
      steps.add(step);
    }
    when(jobExecution.getStepExecutions()).thenReturn(steps);
    return jobExecution;
  }

  private JobParameters executionRoundParams(int round) {
    return new JobParameters(Set.of(
      new JobParameter<>(JOB_PARAM_EXECUTION_ROUND, round, Integer.class)
    ));
  }

}




