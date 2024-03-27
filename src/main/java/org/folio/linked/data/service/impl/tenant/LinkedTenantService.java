package org.folio.linked.data.service.impl.tenant;

import java.util.Collection;
import lombok.extern.log4j.Log4j2;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.liquibase.FolioSpringLiquibase;
import org.folio.spring.service.TenantService;
import org.folio.tenant.domain.dto.TenantAttributes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Log4j2
@Primary
@Service
public class LinkedTenantService extends TenantService {

  private final Collection<TenantServiceWorker> workers;

  @Autowired
  public LinkedTenantService(
    JdbcTemplate jdbcTemplate,
    FolioExecutionContext context,
    FolioSpringLiquibase folioSpringLiquibase,
    Collection<TenantServiceWorker> workers
  ) {
    super(jdbcTemplate, context, folioSpringLiquibase);
    this.workers = workers;
  }

  @Override
  public void beforeTenantUpdate(TenantAttributes tenantAttributes) {
    log.debug("Start before actions for the tenant [{}]", context.getTenantId());
    workers.forEach(worker -> worker.beforeTenantUpdate(tenantAttributes));
  }

  @Override
  public void afterTenantUpdate(TenantAttributes tenantAttributes) {
    log.info("Start after actions for the tenant [{}]", context.getTenantId());
    workers.forEach(worker -> worker.afterTenantUpdate(tenantAttributes));
  }
}
