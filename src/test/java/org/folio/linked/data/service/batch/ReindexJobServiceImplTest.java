package org.folio.linked.data.service.batch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.linked.data.configuration.batch.BatchConfig.JOB_PARAM_IS_FULL_REINDEX;
import static org.folio.linked.data.configuration.batch.BatchConfig.JOB_PARAM_RESOURCE_TYPE;
import static org.folio.linked.data.configuration.batch.BatchConfig.JOB_PARAM_RUN_TIMESTAMP;
import static org.folio.linked.data.configuration.batch.BatchConfig.JOB_PARAM_STARTED_BY;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.UUID;
import org.folio.linked.data.exception.RequestProcessingException;
import org.folio.linked.data.exception.RequestProcessingExceptionBuilder;
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
import org.springframework.batch.core.job.JobInstance;
import org.springframework.batch.core.job.parameters.InvalidJobParametersException;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.launch.support.TaskExecutorJobOperator;

@UnitTest
@ExtendWith(MockitoExtension.class)
class ReindexJobServiceImplTest {

  @InjectMocks
  private ReindexJobServiceImpl reindexJobService;
  @Mock
  private Job reindexJob;
  @Mock
  private TaskExecutorJobOperator jobOperator;
  @Mock
  private FolioExecutionContext folioExecutionContext;
  @Mock
  private RequestProcessingExceptionBuilder exceptionBuilder;

  @Test
  void start_shouldStartJobWithFullReindexAndResourceType() throws JobExecutionException {
    // given
    var userId = UUID.randomUUID();
    var expectedJobInstanceId = 123L;
    var jobInstance = new JobInstance(expectedJobInstanceId, "reindexJob");
    var jobExecution = new JobExecution(1L, jobInstance, new JobParameters());
    when(folioExecutionContext.getUserId()).thenReturn(userId);
    when(jobOperator.start(eq(reindexJob), any(JobParameters.class))).thenReturn(jobExecution);

    // when
    var result = reindexJobService.start(true, "WORK");

    // then
    assertThat(result).isEqualTo(expectedJobInstanceId);
    verify(jobOperator).start(eq(reindexJob), argThat(params -> {
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
  void start_shouldStartJobWithoutResourceType() throws JobExecutionException {
    // given
    var userId = UUID.randomUUID();
    var expectedJobInstanceId = 456L;
    var jobInstance = new JobInstance(expectedJobInstanceId, "reindexJob");
    var jobExecution = new JobExecution(2L, jobInstance, new JobParameters());
    when(folioExecutionContext.getUserId()).thenReturn(userId);
    when(jobOperator.start(eq(reindexJob), any(JobParameters.class))).thenReturn(jobExecution);

    // when
    var result = reindexJobService.start(false, null);


    // then
    assertThat(result).isEqualTo(expectedJobInstanceId);
    verify(jobOperator).start(eq(reindexJob), argThat(params -> {
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
  void start_shouldUseUnknownWhenUserIdIsNull() throws JobExecutionException {
    // given
    var expectedJobInstanceId = 789L;
    var jobInstance = new JobInstance(expectedJobInstanceId, "reindexJob");
    var jobExecution = new JobExecution(3L, jobInstance, new JobParameters());
    when(folioExecutionContext.getUserId()).thenReturn(null);
    when(jobOperator.start(eq(reindexJob), any(JobParameters.class))).thenReturn(jobExecution);

    // when
    var result = reindexJobService.start(true, "HUB");

    // then
    assertThat(result).isEqualTo(expectedJobInstanceId);
    verify(jobOperator).start(eq(reindexJob), argThat(params -> {
      var startedBy = params.getString(JOB_PARAM_STARTED_BY);
      return "unknown".equals(startedBy);
    }));
  }

  @Test
  void start_shouldThrowExceptionForUnsupportedResourceType() throws Exception {
    // given
    var userId = UUID.randomUUID();
    when(folioExecutionContext.getUserId()).thenReturn(userId);
    var expectedException = new RequestProcessingException(400, "Bad request", Map.of(),
      "Not supported resource type: INSTANCE");
    when(exceptionBuilder.badRequestException("Not supported resource type: INSTANCE", "Bad request"))
      .thenReturn(expectedException);

    // when
    var thrown = assertThrows(RequestProcessingException.class,
      () -> reindexJobService.start(true, "INSTANCE"));

    // then
    assertThat(thrown).isEqualTo(expectedException);
    verify(jobOperator, never()).start(eq(reindexJob), any(JobParameters.class));
  }

  @Test
  void start_shouldThrowExceptionForInvalidResourceType() throws Exception {
    // given
    var userId = UUID.randomUUID();
    when(folioExecutionContext.getUserId()).thenReturn(userId);
    var expectedException = new RequestProcessingException(400, "Bad request", Map.of(),
      "Not supported resource type: INVALID");
    when(exceptionBuilder.badRequestException("Not supported resource type: INVALID", "Bad request"))
      .thenReturn(expectedException);

    // when
    var thrown = assertThrows(RequestProcessingException.class,
      () -> reindexJobService.start(false, "INVALID"));

    // then
    assertThat(thrown).isEqualTo(expectedException);
    verify(jobOperator, never()).start(eq(reindexJob), any(JobParameters.class));
  }

  @Test
  void start_shouldThrowIllegalArgumentExceptionWhenJobExecutionFails() throws JobExecutionException {
    // given
    var userId = UUID.randomUUID();
    when(folioExecutionContext.getUserId()).thenReturn(userId);
    when(jobOperator.start(eq(reindexJob), any(JobParameters.class)))
      .thenThrow(new InvalidJobParametersException("Job execution failed"));

    // when
    var thrown = assertThrows(IllegalArgumentException.class,
      () -> reindexJobService.start(true, "WORK"));

    // then
    assertThat(thrown.getMessage()).isEqualTo("Job launch exception");
    assertThat(thrown.getCause()).isInstanceOf(JobExecutionException.class);
  }

}
