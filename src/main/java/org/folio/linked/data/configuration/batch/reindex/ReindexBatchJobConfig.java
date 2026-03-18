package org.folio.linked.data.configuration.batch.reindex;

import static java.lang.Boolean.parseBoolean;
import static org.folio.ld.dictionary.ResourceTypeDictionary.HUB;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.folio.linked.data.util.Constants.STANDALONE_PROFILE;

import java.util.Set;
import javax.sql.DataSource;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.configuration.batch.reindex.reader.ReindexResourceReader;
import org.folio.linked.data.domain.dto.ResourceIndexEvent;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.support.SynchronizedItemStreamReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@Profile("!" + STANDALONE_PROFILE)
public class ReindexBatchJobConfig {

  public static final Set<ResourceTypeDictionary> SUPPORTED_TYPES = Set.of(HUB, WORK);
  public static final String JOB_PARAM_STARTED_BY = "startedBy";
  public static final String JOB_PARAM_IS_FULL_REINDEX = "isFullReindex";
  public static final String JOB_PARAM_RESOURCE_TYPE = "resourceType";
  public static final String REINDEX_STEP_NAME = "reindexStep";

  @Bean
  public Job reindexJob(JobRepository jobRepository,
                        Step dropIndexStep,
                        Step reindexStep) {
    return new JobBuilder("reindexJob", jobRepository)
      .start(dropIndexStep)
      .next(reindexStep)
      .build();
  }

  @Bean
  public Step dropIndexStep(JobRepository jobRepository,
                            DropIndexTasklet dropIndexTasklet,
                            PlatformTransactionManager transactionManager) {
    return new StepBuilder("dropIndexStep", jobRepository)
      .tasklet(dropIndexTasklet, transactionManager)
      .build();
  }

  @Bean
  public Step reindexStep(JobRepository jobRepository,
                          SynchronizedItemStreamReader<Resource> reindexResourceReader,
                          ReindexProcessor reindexProcessor,
                          ReindexWriter reindexWriter,
                          @Value("${mod-linked-data.reindex.chunk-size}") int chunkSize,
                          AsyncTaskExecutor taskExecutor) {
    return new StepBuilder(REINDEX_STEP_NAME, jobRepository)
      .<Resource, ResourceIndexEvent>chunk(chunkSize)
      .reader(reindexResourceReader)
      .processor(reindexProcessor)
      .writer(reindexWriter)
      .taskExecutor(taskExecutor)
      .build();
  }

  @Bean
  @StepScope
  public SynchronizedItemStreamReader<Resource> reindexResourceReader(
    @Value("#{jobParameters['" + JOB_PARAM_IS_FULL_REINDEX + "']}") String isFullReindex,
    @Value("#{jobParameters['" + JOB_PARAM_RESOURCE_TYPE + "']}") String resourceType,
    @Value("${mod-linked-data.reindex.chunk-size}") int chunkSize,
    DataSource dataSource
  ) {
    var isFullReindexBool = parseBoolean(isFullReindex);
    var reader = new ReindexResourceReader(dataSource, chunkSize, isFullReindexBool, resourceType);
    return new SynchronizedItemStreamReader<>(reader);
  }

}
