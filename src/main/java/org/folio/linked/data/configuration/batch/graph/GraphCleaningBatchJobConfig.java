package org.folio.linked.data.configuration.batch.graph;

import javax.sql.DataSource;
import org.folio.linked.data.configuration.batch.graph.reader.GraphCleaningReader;
import org.folio.linked.data.service.batch.BatchJobService;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.support.SynchronizedItemStreamReader;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;

@Configuration
public class GraphCleaningBatchJobConfig {

  public static final String GRAPH_CLEANING_JOB = "graphCleaningJob";
  public static final String JOB_PARAM_EXECUTION_ROUND = "executionRound";
  public static final String GRAPH_CLEANING_STEP_NAME = "graphCleaningStep";

  @Bean
  public Job graphCleaningJob(JobRepository jobRepository,
                              Step graphCleaningStep,
                              GraphCleaningJobListener graphCleaningJobListener) {
    return new JobBuilder(GRAPH_CLEANING_JOB, jobRepository)
      .start(graphCleaningStep)
      .listener(graphCleaningJobListener)
      .build();
  }

  @Bean
  public GraphCleaningJobListener graphCleaningJobListener(ObjectProvider<BatchJobService> batchJobServiceProvider) {
    return new GraphCleaningJobListener(batchJobServiceProvider);
  }

  @Bean
  public Step graphCleaningStep(JobRepository jobRepository,
                                SynchronizedItemStreamReader<Long> graphCleaningReader,
                                GraphCleaningWriter graphCleaningWriter,
                                @Value("${mod-linked-data.graph-cleaning.chunk-size}") int chunkSize,
                                AsyncTaskExecutor taskExecutor) {
    return new StepBuilder(GRAPH_CLEANING_STEP_NAME, jobRepository)
      .<Long, Long>chunk(chunkSize)
      .reader(graphCleaningReader)
      .writer(graphCleaningWriter)
      .taskExecutor(taskExecutor)
      .build();
  }

  @Bean
  @StepScope
  public SynchronizedItemStreamReader<Long> graphCleaningResourceReader(
    @Value("${mod-linked-data.graph-cleaning.chunk-size}") int chunkSize,
    DataSource dataSource
  ) {
    return new SynchronizedItemStreamReader<>(new GraphCleaningReader(dataSource, chunkSize));
  }

}
