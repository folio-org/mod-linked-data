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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.folio.linked.data.domain.dto.ReindexJobStatusDto;
import org.folio.linked.data.exception.RequestProcessingException;
import org.folio.linked.data.exception.RequestProcessingExceptionBuilder;
import org.folio.linked.data.mapper.dto.ReindexJobStatusMapper;
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
  @Mock
  private BatchJobExecutionRepository batchJobExecutionRepository;
  @Mock
  private ReindexJobStatusMapper reindexJobStatusMapper;

  @Test
  void start_shouldStartJobWithFullReindexAndResourceType() throws JobExecutionException {
    // given
    var userId = UUID.randomUUID();
    var expectedJobExecutionId = 1L;
    var jobExecution = mock(JobExecution.class);
    when(jobExecution.getId()).thenReturn(expectedJobExecutionId);
    when(folioExecutionContext.getUserId()).thenReturn(userId);
    when(jobOperator.start(eq(reindexJob), any(JobParameters.class))).thenReturn(jobExecution);

    // when
    var result = reindexJobService.start(true, "WORK");

    // then
    assertThat(result).isEqualTo(expectedJobExecutionId);
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
    var expectedJobExecutionId = 2L;
    var jobExecution = mock(JobExecution.class);
    when(jobExecution.getId()).thenReturn(expectedJobExecutionId);
    when(folioExecutionContext.getUserId()).thenReturn(userId);
    when(jobOperator.start(eq(reindexJob), any(JobParameters.class))).thenReturn(jobExecution);

    // when
    var result = reindexJobService.start(false, null);

    // then
    assertThat(result).isEqualTo(expectedJobExecutionId);
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
    var expectedJobExecutionId = 3L;
    var jobExecution = mock(JobExecution.class);
    when(jobExecution.getId()).thenReturn(expectedJobExecutionId);
    when(folioExecutionContext.getUserId()).thenReturn(null);
    when(jobOperator.start(eq(reindexJob), any(JobParameters.class))).thenReturn(jobExecution);

    // when
    var result = reindexJobService.start(true, "HUB");

    // then
    assertThat(result).isEqualTo(expectedJobExecutionId);
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

  @Test
  void getStatus_shouldReturnDto_whenJobExecutionFound() {
    // given
    var jobExecutionId = 42L;
    var execution = new BatchJobExecution();
    var expectedDto = new ReindexJobStatusDto().status("COMPLETED");
    when(batchJobExecutionRepository.findById(jobExecutionId)).thenReturn(Optional.of(execution));
    when(reindexJobStatusMapper.toDto(execution)).thenReturn(expectedDto);

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
