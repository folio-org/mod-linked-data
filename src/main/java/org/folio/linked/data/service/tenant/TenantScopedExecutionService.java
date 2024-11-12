package org.folio.linked.data.service.tenant;

import static java.util.stream.Collectors.toMap;
import static org.folio.spring.tools.config.RetryTemplateConfiguration.DEFAULT_KAFKA_RETRY_TEMPLATE_NAME;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.scope.FolioExecutionContextSetter;
import org.folio.spring.tools.context.ExecutionContextBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.MessageHeaders;
import org.springframework.retry.RetryContext;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TenantScopedExecutionService {
  @Qualifier(DEFAULT_KAFKA_RETRY_TEMPLATE_NAME)
  private final RetryTemplate retryTemplate;
  private final ExecutionContextBuilder contextBuilder;

  @SneakyThrows
  public <T> T execute(String tenantId, Callable<T> job) {
    try (var fex = new FolioExecutionContextSetter(dbOnlyContext(tenantId))) {
      return job.call();
    }
  }

  public void execute(String tenantId, Runnable job) {
    try (var fex = new FolioExecutionContextSetter(dbOnlyContext(tenantId))) {
      job.run();
    }
  }

  @Async
  public void executeAsyncWithRetry(Headers headers, Consumer<RetryContext> job, Consumer<Throwable> failureHandler) {
    try (var fex = new FolioExecutionContextSetter(kafkaFolioExecutionContext(headers))) {
      retryTemplate.execute(
        context -> runJob(job, context),
        context -> handleError(failureHandler, context)
      );
    }
  }

  private boolean runJob(Consumer<RetryContext> job, RetryContext context) {
    job.accept(context);
    return true;
  }

  private boolean handleError(Consumer<Throwable> failureHandler, RetryContext context) {
    failureHandler.accept(context.getLastThrowable());
    return false;
  }

  private FolioExecutionContext kafkaFolioExecutionContext(Headers headers) {
    var headersMap = Arrays.stream(headers.toArray())
      .collect(toMap(Header::key, Header::value, (o, o2) -> o2, (Supplier<Map<String, Object>>) HashMap::new));
    return contextBuilder.forMessageHeaders(new MessageHeaders(headersMap));
  }

  public FolioExecutionContext dbOnlyContext(String tenantId) {
    return contextBuilder.builder().withTenantId(tenantId).build();
  }

}
