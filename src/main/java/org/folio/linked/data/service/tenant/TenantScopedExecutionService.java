package org.folio.linked.data.service.tenant;

import static org.folio.linked.data.util.Constants.FOLIO_PROFILE;

import java.util.concurrent.Callable;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.scope.FolioExecutionContextSetter;
import org.folio.spring.tools.context.ExecutionContextBuilder;
import org.folio.spring.tools.systemuser.SystemUserService;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Profile(FOLIO_PROFILE)
@RequiredArgsConstructor
public class TenantScopedExecutionService {
  private final ExecutionContextBuilder contextBuilder;
  private final SystemUserService systemUserService;

  @SneakyThrows
  public <T> T executeTenantScoped(String tenantId, Callable<T> job) {
    try (var fex = new FolioExecutionContextSetter(systemUserfolioExecutionContext(tenantId))) {
      return job.call();
    }
  }

  @Async
  public void executeAsyncTenantScoped(String tenantId, Runnable job) {
    try (var fex = new FolioExecutionContextSetter(systemUserfolioExecutionContext(tenantId))) {
      job.run();
    }
  }

  private FolioExecutionContext systemUserfolioExecutionContext(String tenant) {
    return contextBuilder.forSystemUser(systemUserService.getAuthedSystemUser(tenant));
  }

}
