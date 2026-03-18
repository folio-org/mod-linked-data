package org.folio.linked.data.service.batch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.linked.data.configuration.batch.BatchConfig.JOB_PARAM_RUN_TIMESTAMP;
import static org.folio.linked.data.configuration.batch.graph.GraphCleaningBatchJobConfig.GRAPH_CLEANING_STEP_NAME;
import static org.folio.linked.data.configuration.batch.graph.GraphCleaningBatchJobConfig.JOB_PARAM_EXECUTION_ROUND;
import static org.folio.linked.data.configuration.batch.reindex.ReindexBatchJobConfig.JOB_PARAM_IS_FULL_REINDEX;
import static org.folio.linked.data.configuration.batch.reindex.ReindexBatchJobConfig.JOB_PARAM_RESOURCE_TYPE;
import static org.folio.linked.data.configuration.batch.reindex.ReindexBatchJobConfig.JOB_PARAM_STARTED_BY;
import static org.folio.linked.data.configuration.batch.reindex.ReindexBatchJobConfig.REINDEX_STEP_NAME;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.folio.linked.data.domain.dto.BatchJobStatusDto;
import org.folio.linked.data.exception.RequestProcessingException;
import org.folio.linked.data.exception.RequestProcessingExceptionBuilder;
import org.folio.linked.data.mapper.dto.BatchJobStatusMapper;
import org.folio.linked.data.model.entity.batch.BatchJobExecution;
import org.folio.linked.data.repo.BatchJobExecutionRepository;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.JobExecutionException;
import org.springframework.batch.core.job.parameters.InvalidJobParametersException;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.launch.support.TaskExecutorJobOperator;

@UnitTest
@ExtendWith(MockitoExtension.class)
class BatchJobServiceImplTest {

  @InjectMocks
  private BatchJobServiceImpl reindexJobService;
  @Mock
  private Job reindexJob;
  @Mock
  private Job graphCleaningJob;
  @Mock
  private TaskExecutorJobOperator jobOperator;
  @Mock
  private FolioExecutionContext folioExecutionContext;
  @Mock
  private RequestProcessingExceptionBuilder exceptionBuilder;
  @Mock
  private BatchJobExecutionRepository batchJobExecutionRepository;
  @Mock
  private BatchJobStatusMapper batchJobStatusMapper;

  @Test
  void startReindex_shouldStartReindexJobWithFullReindexAndResourceType() throws JobExecutionException {
    // given
    var userId = UUID.randomUUID();
    var expectedJobExecutionId = 1L;
    var jobExecution = mock(JobExecution.class);
    when(jobExecution.getId()).thenReturn(expectedJobExecutionId);
    when(folioExecutionContext.getUserId()).thenReturn(userId);
    doReturn(jobExecution).when(jobOperator).start(any(Job.class), any(JobParameters.class));

    // when
    var result = reindexJobService.startReindex(true, "WORK");

    // then
    assertThat(result).isEqualTo(expectedJobExecutionId);
    verify(jobOperator).start(any(Job.class), argThat(params -> {
      var isFullReindex = params.getString(JOB_PARAM_IS_FULL_REINDEX);
      var resourceType = params.getString(JOB_PARAM_RESOURCE_TYPE);
      var startedBy = params.getString(JOB_PARAM_STARTED_BY);
      var timestamp = params.getLong(JOB_PARAM_RUN_TIMESTAMP);
      return "true".equals(isFullReindex)
        && "WORK".equals(resourceType)
        && userId.toString().equals(startedBy)
        && timestamp != null;
    }));
  }

  @Test
  void startReindex_shouldStartReindexJobWithoutResourceType() throws JobExecutionException {
    // given
    var userId = UUID.randomUUID();
    var expectedJobExecutionId = 2L;
    var jobExecution = mock(JobExecution.class);
    when(jobExecution.getId()).thenReturn(expectedJobExecutionId);
    when(folioExecutionContext.getUserId()).thenReturn(userId);
    doReturn(jobExecution).when(jobOperator).start(any(Job.class), any(JobParameters.class));

    // when
    var result = reindexJobService.startReindex(false, null);

    // then
    assertThat(result).isEqualTo(expectedJobExecutionId);
    verify(jobOperator).start(any(Job.class), argThat(params -> {
      var isFullReindex = params.getString(JOB_PARAM_IS_FULL_REINDEX);
      var resourceType = params.getString(JOB_PARAM_RESOURCE_TYPE);
      var startedBy = params.getString(JOB_PARAM_STARTED_BY);
      var timestamp = params.getLong(JOB_PARAM_RUN_TIMESTAMP);
      return "false".equals(isFullReindex)
        && resourceType == null
        && userId.toString().equals(startedBy)
        && timestamp != null;
    }));
  }

  @Test
  void startReindex_shouldUseUnknownWhenUserIdIsNull() throws JobExecutionException {
    // given
    var expectedJobExecutionId = 3L;
    var jobExecution = mock(JobExecution.class);
    when(jobExecution.getId()).thenReturn(expectedJobExecutionId);
    when(folioExecutionContext.getUserId()).thenReturn(null);
    doReturn(jobExecution).when(jobOperator).start(any(Job.class), any(JobParameters.class));

    // when
    var result = reindexJobService.startReindex(true, "HUB");

    // then
    assertThat(result).isEqualTo(expectedJobExecutionId);
    verify(jobOperator).start(any(Job.class), argThat(params -> {
      var startedBy = params.getString(JOB_PARAM_STARTED_BY);
      return "unknown".equals(startedBy);
    }));
  }

  @Test
  void startReindex_shouldThrowExceptionForUnsupportedResourceType() throws Exception {
    // given
    var userId = UUID.randomUUID();
    when(folioExecutionContext.getUserId()).thenReturn(userId);
    var expectedException = new RequestProcessingException(400, "Bad request", Map.of(),
      "Not supported resource type: INSTANCE");
    when(exceptionBuilder.badRequestException("Not supported resource type: INSTANCE", "Bad request"))
      .thenReturn(expectedException);

    // when
    var thrown = assertThrows(RequestProcessingException.class,
      () -> reindexJobService.startReindex(true, "INSTANCE"));

    // then
    assertThat(thrown).isEqualTo(expectedException);
    verify(jobOperator, never()).start(any(Job.class), any(JobParameters.class));
  }

  @Test
  void startReindex_shouldThrowExceptionForInvalidResourceType() throws Exception {
    // given
    var userId = UUID.randomUUID();
    when(folioExecutionContext.getUserId()).thenReturn(userId);
    var expectedException = new RequestProcessingException(400, "Bad request", Map.of(),
      "Not supported resource type: INVALID");
    when(exceptionBuilder.badRequestException("Not supported resource type: INVALID", "Bad request"))
      .thenReturn(expectedException);

    // when
    var thrown = assertThrows(RequestProcessingException.class,
      () -> reindexJobService.startReindex(false, "INVALID"));

    // then
    assertThat(thrown).isEqualTo(expectedException);
    verify(jobOperator, never()).start(any(Job.class), any(JobParameters.class));
  }

  @Test
  void startReindex_shouldThrowIllegalArgumentExceptionWhenJobExecutionFails() throws JobExecutionException {
    // given
    var userId = UUID.randomUUID();
    when(folioExecutionContext.getUserId()).thenReturn(userId);
    doThrow(new InvalidJobParametersException("Job execution failed"))
      .when(jobOperator).start(any(Job.class), any(JobParameters.class));

    // when
    var thrown = assertThrows(IllegalArgumentException.class,
      () -> reindexJobService.startReindex(true, "WORK"));

    // then
    assertThat(thrown.getMessage()).isEqualTo("Reindex Job launch exception");
    assertThat(thrown.getCause()).isInstanceOf(JobExecutionException.class);
  }

  @Test
  void startGraphCleaning_shouldStartGraphCleaningJobWithExecutionRound() throws JobExecutionException {
    // given
    var executionRound = 1;
    var expectedJobExecutionId = 10L;
    var jobExecution = mock(JobExecution.class);
    when(jobExecution.getId()).thenReturn(expectedJobExecutionId);
    doReturn(jobExecution).when(jobOperator).start(any(Job.class), any(JobParameters.class));

    // when
    var result = reindexJobService.startGraphCleaning(executionRound);

    // then
    assertThat(result).isEqualTo(expectedJobExecutionId);
    verify(jobOperator).start(any(Job.class), argThat(params -> {
      var roundParam = params.getParameter(JOB_PARAM_EXECUTION_ROUND);
      var timestamp = params.getLong(JOB_PARAM_RUN_TIMESTAMP);
      return roundParam != null && executionRound == (Integer) roundParam.value() && timestamp != null;
    }));
  }

  @Test
  void startGraphCleaning_shouldNotPassReindexParams() throws JobExecutionException {
    // given
    var executionRound = 2;
    var jobExecution = mock(JobExecution.class);
    when(jobExecution.getId()).thenReturn(11L);
    doReturn(jobExecution).when(jobOperator).start(any(Job.class), any(JobParameters.class));

    // when
    reindexJobService.startGraphCleaning(executionRound);

    // then
    verify(jobOperator).start(any(Job.class), argThat(params ->
      params.getString(JOB_PARAM_IS_FULL_REINDEX) == null
        && params.getString(JOB_PARAM_STARTED_BY) == null
        && params.getString(JOB_PARAM_RESOURCE_TYPE) == null
    ));
  }

  @Test
  void startGraphCleaning_shouldThrowIllegalArgumentExceptionWhenJobExecutionFails() throws JobExecutionException {
    // given
    doThrow(new InvalidJobParametersException("Job execution failed"))
      .when(jobOperator).start(any(Job.class), any(JobParameters.class));

    // when
    var thrown = assertThrows(IllegalArgumentException.class,
      () -> reindexJobService.startGraphCleaning(1));

    // then
    assertThat(thrown.getMessage()).isEqualTo("GraphCleaning Job launch exception");
    assertThat(thrown.getCause()).isInstanceOf(JobExecutionException.class);
  }

  @Test
  void getStatus_shouldUseGraphCleaningStepName_whenExecutionRoundIsPresent() {
    // given
    var jobExecutionId = 55L;
    var execution = new BatchJobExecution();
    execution.setExecutionRound(1);
    var expectedDto = new BatchJobStatusDto().status("COMPLETED");
    when(batchJobExecutionRepository.findById(jobExecutionId)).thenReturn(Optional.of(execution));
    when(batchJobStatusMapper.toDto(execution, GRAPH_CLEANING_STEP_NAME)).thenReturn(expectedDto);

    // when
    var result = reindexJobService.getStatus(jobExecutionId);

    // then
    assertThat(result).isEqualTo(expectedDto);
    verify(batchJobStatusMapper).toDto(execution, GRAPH_CLEANING_STEP_NAME);
  }

  @Test
  void getStatus_shouldReturnDto_whenJobExecutionFound() {
    // given
    var jobExecutionId = 42L;
    var execution = new BatchJobExecution();
    var expectedDto = new BatchJobStatusDto().status("COMPLETED");
    when(batchJobExecutionRepository.findById(jobExecutionId)).thenReturn(Optional.of(execution));
    when(batchJobStatusMapper.toDto(execution, REINDEX_STEP_NAME)).thenReturn(expectedDto);

    // when
    var result = reindexJobService.getStatus(jobExecutionId);

    // then
    assertThat(result).isEqualTo(expectedDto);
  }

  @Test
  void getStatus_shouldThrowNotFoundException_whenJobExecutionNotFound() {
    // given
    var jobExecutionId = 99L;
    var expectedException = new RequestProcessingException(404, "Not found", Map.of(), "JobExecution not found");
    when(batchJobExecutionRepository.findById(jobExecutionId)).thenReturn(Optional.empty());
    when(exceptionBuilder.notFoundLdResourceByIdException("JobExecution", "99")).thenReturn(expectedException);

    // when
    var thrown = assertThrows(RequestProcessingException.class,
      () -> reindexJobService.getStatus(jobExecutionId));

    // then
    assertThat(thrown).isEqualTo(expectedException);
  }

}
