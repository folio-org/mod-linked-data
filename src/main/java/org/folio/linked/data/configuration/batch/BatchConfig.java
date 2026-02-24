package org.folio.linked.data.configuration.batch;

import static java.lang.Boolean.parseBoolean;
import static org.folio.ld.dictionary.ResourceTypeDictionary.HUB;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.folio.linked.data.util.Constants.STANDALONE_PROFILE;

import jakarta.persistence.EntityManagerFactory;
import java.util.Set;
import javax.sql.DataSource;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.domain.dto.ResourceIndexEvent;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.configuration.support.MapJobRegistry;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.TaskExecutorJobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JdbcJobRepositoryFactoryBean;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.support.SynchronizedItemStreamReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@Profile("!" + STANDALONE_PROFILE)
public class BatchConfig {

  public static final Set<ResourceTypeDictionary> SUPPORTED_TYPES = Set.of(HUB, WORK);
  public static final String JOB_PARAM_IS_FULL_REINDEX = "isFullReindex";
  public static final String JOB_PARAM_RESOURCE_TYPE = "resourceType";
  public static final String JOB_PARAM_STARTED_BY = "startedBy";
  public static final String JOB_PARAM_RUN_TIMESTAMP = "run.timestamp";

  @Bean
  public BeanFactoryPostProcessor jobAndStepScopeConfigurer() {
    return beanFactory -> {
      var configurableBeanFactory = (ConfigurableBeanFactory) beanFactory;
      configurableBeanFactory.registerScope("job", new org.springframework.batch.core.scope.JobScope());
      configurableBeanFactory.registerScope("step", new org.springframework.batch.core.scope.StepScope());
    };
  }

  @Bean
  public JobRepository jobRepository(DataSource dataSource,
                                     PlatformTransactionManager transactionManager) throws Exception {
    var factory = new JdbcJobRepositoryFactoryBean();
    factory.setDataSource(dataSource);
    factory.setTransactionManager(transactionManager);
    factory.setTablePrefix("batch_");
    factory.afterPropertiesSet();
    return factory.getObject();
  }

  @Bean
  public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
    return new JpaTransactionManager(emf);
  }

  @Bean
  public AsyncTaskExecutor taskExecutor() {
    var simpleAsyncTaskExecutor = new SimpleAsyncTaskExecutor();
    simpleAsyncTaskExecutor.setVirtualThreads(true);
    return simpleAsyncTaskExecutor;
  }

  @Bean
  public TaskExecutorJobOperator taskExecutorJobOperator(JobRepository jobRepository,
                                                         JobRegistry jobRegistry,
                                                         AsyncTaskExecutor taskExecutor) throws Exception {
    var jobOperator = new TaskExecutorJobOperator();
    jobOperator.setJobRepository(jobRepository);
    jobOperator.setJobRegistry(jobRegistry);
    jobOperator.setTaskExecutor(taskExecutor);
    jobOperator.afterPropertiesSet();
    return jobOperator;
  }

  @Bean
  public MapJobRegistry jobRegistry() {
    return new MapJobRegistry();
  }

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
  public AsyncTaskExecutor reindexTaskExecutor(
    @Value("${mod-linked-data.reindex.pool-size}") int poolSize
  ) {
    var exec = new ThreadPoolTaskExecutor();
    exec.setMaxPoolSize(poolSize);
    exec.setQueueCapacity(poolSize);
    exec.setThreadNamePrefix("reindex-");
    exec.initialize();
    return exec;
  }

  @Bean
  public Step reindexStep(JobRepository jobRepository,
                          SynchronizedItemStreamReader<Resource> resourceReader,
                          ReindexProcessor reindexProcessor,
                          ReindexWriter reindexWriter,
                          @Value("${mod-linked-data.reindex.chunk-size}") int chunkSize,
                          AsyncTaskExecutor reindexTaskExecutor) {
    return new StepBuilder("reindexStep", jobRepository)
      .<Resource, ResourceIndexEvent>chunk(chunkSize)
      .reader(resourceReader)
      .processor(reindexProcessor)
      .writer(reindexWriter)
      .taskExecutor(reindexTaskExecutor)
      .build();
  }

  @Bean
  @StepScope
  public SynchronizedItemStreamReader<Resource> resourceReader(
    @Value("#{jobParameters['" + JOB_PARAM_IS_FULL_REINDEX + "']}") String isFullReindex,
    @Value("#{jobParameters['" + JOB_PARAM_RESOURCE_TYPE + "']}") String resourceType,
    @Value("${mod-linked-data.reindex.chunk-size}") int chunkSize,
    EntityManagerFactory entityManagerFactory
  ) {
    var isFullReindexBool = parseBoolean(isFullReindex);
    var reader = new ResourceReader(entityManagerFactory, chunkSize, isFullReindexBool, resourceType);
    return new SynchronizedItemStreamReader<>(reader);
  }

}
