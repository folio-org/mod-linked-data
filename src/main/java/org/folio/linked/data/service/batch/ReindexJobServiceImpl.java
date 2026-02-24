package org.folio.linked.data.service.batch;

import static java.lang.String.valueOf;
import static java.util.Optional.ofNullable;
import static org.folio.linked.data.configuration.batch.BatchConfig.JOB_PARAM_IS_FULL_REINDEX;
import static org.folio.linked.data.configuration.batch.BatchConfig.JOB_PARAM_RESOURCE_TYPE;
import static org.folio.linked.data.configuration.batch.BatchConfig.JOB_PARAM_RUN_TIMESTAMP;
import static org.folio.linked.data.configuration.batch.BatchConfig.JOB_PARAM_STARTED_BY;
import static org.folio.linked.data.configuration.batch.BatchConfig.SUPPORTED_TYPES;
import static org.folio.linked.data.util.Constants.STANDALONE_PROFILE;

import java.util.HashSet;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.exception.RequestProcessingExceptionBuilder;
import org.folio.spring.FolioExecutionContext;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecutionException;
import org.springframework.batch.core.job.parameters.JobParameter;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.launch.support.TaskExecutorJobOperator;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Profile("!" + STANDALONE_PROFILE)
public class ReindexJobServiceImpl implements ReindexJobService {

  private final Job reindexJob;
  private final TaskExecutorJobOperator jobOperator;
  private final FolioExecutionContext folioExecutionContext;
  private final RequestProcessingExceptionBuilder exceptionBuilder;

  @Override
  public Long start(boolean isFullReindex, String resourceType) {
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
      return jobOperator.start(reindexJob, new JobParameters(params)).getJobInstanceId();
    } catch (JobExecutionException e) {
      throw new IllegalArgumentException("Job launch exception", e);
    }
  }

  private ResourceTypeDictionary getResourceTypeDictionary(String resourceType) {
    try {
      return ResourceTypeDictionary.valueOf(resourceType.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw exceptionBuilder.badRequestException("Not supported resource type: " + resourceType, "Bad request");
    }
  }

}
