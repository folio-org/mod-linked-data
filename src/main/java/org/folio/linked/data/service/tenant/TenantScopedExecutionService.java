package org.folio.linked.data.service.tenant;

import static java.util.stream.Collectors.toMap;
import static org.folio.linked.data.util.Constants.FOLIO_PROFILE;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.scope.FolioExecutionContextSetter;
import org.folio.spring.tools.context.ExecutionContextBuilder;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.MessageHeaders;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Profile(FOLIO_PROFILE)
@RequiredArgsConstructor
public class TenantScopedExecutionService {
  private final ExecutionContextBuilder contextBuilder;

  @SneakyThrows
  public <T> T executeTenantScoped(String tenantId, Callable<T> job) {
    try (var fex = new FolioExecutionContextSetter(dbOnlyContext(tenantId))) {
      return job.call();
    }
  }

  @Async
  public void executeAsyncTenantScoped(Headers headers, Runnable job) {
    try (var fex = new FolioExecutionContextSetter(kafkaFolioExecutionContext(headers))) {
      job.run();
    }
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
