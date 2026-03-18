package org.folio.linked.data.configuration.batch;

import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.support.MapJobRegistry;
import org.springframework.batch.core.launch.support.TaskExecutorJobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JdbcJobRepositoryFactoryBean;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class BatchConfig {

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

}
