package org.folio.linked.data.service.batch;

import static java.lang.String.valueOf;
import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static org.folio.linked.data.configuration.batch.BatchConfig.JOB_PARAM_RUN_TIMESTAMP;
import static org.folio.linked.data.configuration.batch.graph.GraphCleaningBatchJobConfig.GRAPH_CLEANING_JOB;
import static org.folio.linked.data.configuration.batch.graph.GraphCleaningBatchJobConfig.GRAPH_CLEANING_STEP_NAME;
import static org.folio.linked.data.configuration.batch.graph.GraphCleaningBatchJobConfig.JOB_PARAM_EXECUTION_ROUND;
import static org.folio.linked.data.configuration.batch.reindex.ReindexBatchJobConfig.JOB_PARAM_IS_FULL_REINDEX;
import static org.folio.linked.data.configuration.batch.reindex.ReindexBatchJobConfig.JOB_PARAM_RESOURCE_TYPE;
import static org.folio.linked.data.configuration.batch.reindex.ReindexBatchJobConfig.JOB_PARAM_STARTED_BY;
import static org.folio.linked.data.configuration.batch.reindex.ReindexBatchJobConfig.REINDEX_JOB;
import static org.folio.linked.data.configuration.batch.reindex.ReindexBatchJobConfig.REINDEX_STEP_NAME;
import static org.folio.linked.data.configuration.batch.reindex.ReindexBatchJobConfig.SUPPORTED_TYPES;

import java.util.HashSet;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.domain.dto.BatchJobStatusDto;
import org.folio.linked.data.exception.RequestProcessingExceptionBuilder;
import org.folio.linked.data.mapper.dto.BatchJobStatusMapper;
import org.folio.linked.data.repo.BatchJobExecutionRepository;
import org.folio.spring.FolioExecutionContext;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecutionException;
import org.springframework.batch.core.job.parameters.JobParameter;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.launch.support.TaskExecutorJobOperator;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BatchJobServiceImpl implements BatchJobService {

  private final Job reindexJob;
  private final Job graphCleaningJob;
  private final TaskExecutorJobOperator jobOperator;
  private final BatchJobStatusMapper batchJobStatusMapper;
  private final FolioExecutionContext folioExecutionContext;
  private final RequestProcessingExceptionBuilder exceptionBuilder;
  private final BatchJobExecutionRepository batchJobExecutionRepository;

  @Override
  public Long startReindex(boolean isFullReindex, String resourceType) {
    var userId = folioExecutionContext.getUserId();
    var params = new HashSet<JobParameter<?>>();
    ofNullable(resourceType)
      .map(this::getResourceTypeDictionary)
      .ifPresent(rtd -> {
        if (SUPPORTED_TYPES.contains(rtd)) {
          params.add(new JobParameter<>(JOB_PARAM_RESOURCE_TYPE, rtd.name(), String.class));
        } else {
          throw exceptionBuilder.badRequestException("Not supported resource type: " + rtd.name(), "Bad request");
        }
      });
    params.add(new JobParameter<>(JOB_PARAM_IS_FULL_REINDEX, valueOf(isFullReindex), String.class));
    params.add(new JobParameter<>(JOB_PARAM_STARTED_BY,
      ofNullable(userId).map(UUID::toString).orElse("unknown"), String.class)
    );
    params.add(new JobParameter<>(JOB_PARAM_RUN_TIMESTAMP, System.currentTimeMillis(), Long.class));

    try {
      return jobOperator.start(reindexJob, new JobParameters(params)).getId();
    } catch (JobExecutionException e) {
      throw new IllegalArgumentException("Reindex Job launch exception", e);
    }
  }

  @Override
  public Long startGraphCleaning(int executionRound) {
    var params = new HashSet<JobParameter<?>>();
    params.add(new JobParameter<>(JOB_PARAM_RUN_TIMESTAMP, System.currentTimeMillis(), Long.class));
    params.add(new JobParameter<>(JOB_PARAM_EXECUTION_ROUND, executionRound, Integer.class));
    try {
      return jobOperator.start(graphCleaningJob, new JobParameters(params)).getId();
    } catch (JobExecutionException e) {
      throw new IllegalArgumentException("GraphCleaning Job launch exception", e);
    }
  }

  @Override
  public BatchJobStatusDto getStatus(Long jobExecutionId) {
    return batchJobExecutionRepository.findById(jobExecutionId)
      .map(execution -> {
        var jobName = isNull(execution.getExecutionRound()) ? REINDEX_JOB : GRAPH_CLEANING_JOB;
        var stepName = isNull(execution.getExecutionRound()) ? REINDEX_STEP_NAME : GRAPH_CLEANING_STEP_NAME;
        return batchJobStatusMapper.toDto(execution, jobName, stepName);
      })
      .orElseThrow(() -> exceptionBuilder.notFoundLdResourceByIdException("JobExecution", valueOf(jobExecutionId)));
  }

  private ResourceTypeDictionary getResourceTypeDictionary(String resourceType) {
    try {
      return ResourceTypeDictionary.valueOf(resourceType.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw exceptionBuilder.badRequestException("Not supported resource type: " + resourceType, "Bad request");
    }
  }

}
