package org.folio.linked.data.service.tenant;

import static org.folio.linked.data.util.Constants.Cache.MODULE_STATE;
import static org.folio.linked.data.util.Constants.STANDALONE_PROFILE;

import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.job.CacheCleaningJob;
import org.folio.linked.data.service.tenant.worker.TenantServiceWorker;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.liquibase.FolioSpringLiquibase;
import org.folio.spring.service.TenantService;
import org.folio.tenant.domain.dto.TenantAttributes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Log4j2
@Primary
@Service
@Profile("!" + STANDALONE_PROFILE)
public class LinkedDataTenantService extends TenantService {

  private final List<TenantServiceWorker> workers;
  private final CacheCleaningJob cacheCleaningJob;
  private final TenantScopedExecutionService tenantScopedExecutionService;

  @Autowired
  public LinkedDataTenantService(
    JdbcTemplate jdbcTemplate,
    FolioExecutionContext context,
    FolioSpringLiquibase folioSpringLiquibase,
    List<TenantServiceWorker> workers,
    CacheCleaningJob cacheCleaningJob,
    TenantScopedExecutionService tenantScopedExecutionService
  ) {
    super(jdbcTemplate, context, folioSpringLiquibase);
    this.workers = workers;
    this.cacheCleaningJob = cacheCleaningJob;
    this.tenantScopedExecutionService = tenantScopedExecutionService;
  }

  @Override
  public void beforeTenantUpdate(TenantAttributes tenantAttributes) {
    log.debug("Start before update actions for the tenant [{}]", context.getTenantId());
    workers.forEach(worker -> worker.beforeTenantUpdate(context.getTenantId(), tenantAttributes));
  }

  @Override
  public void afterTenantUpdate(TenantAttributes tenantAttributes) {
    log.info("Start after update actions for the tenant [{}]", context.getTenantId());
    workers.forEach(worker -> worker.afterTenantUpdate(context.getTenantId(), tenantAttributes));
    cacheCleaningJob.emptyModuleState();
  }

  @Override
  public void afterTenantDeletion(TenantAttributes tenantAttributes) {
    log.info("Start after delete actions for the tenant [{}]", context.getTenantId());
    workers.forEach(worker -> worker.afterTenantDeletion(context.getTenantId()));
    cacheCleaningJob.emptyModuleState();
  }

  @Cacheable(cacheNames = MODULE_STATE)
  public boolean isTenantExists(String tenantId) {
    return tenantScopedExecutionService.execute(tenantId, super::tenantExists);
  }
}
