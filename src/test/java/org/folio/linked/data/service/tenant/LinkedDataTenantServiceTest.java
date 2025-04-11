package org.folio.linked.data.service.tenant;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.folio.linked.data.job.CacheCleaningJob;
import org.folio.linked.data.service.tenant.worker.TenantServiceWorker;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.liquibase.FolioSpringLiquibase;
import org.folio.spring.testing.type.UnitTest;
import org.folio.tenant.domain.dto.TenantAttributes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

@UnitTest
@ExtendWith(MockitoExtension.class)
class LinkedDataTenantServiceTest {

  @Mock
  private JdbcTemplate jdbcTemplate;
  @Mock
  private FolioExecutionContext context;
  @Mock
  private FolioSpringLiquibase folioSpringLiquibase;
  @Mock
  private TenantServiceWorker testWorker;
  @Mock
  private CacheCleaningJob cacheCleaningJob;
  @Mock
  private TenantScopedExecutionService tenantScopedExecutionService;

  private LinkedDataTenantService tenantService;
  private final String tenantId = "tenant-01";

  @BeforeEach
  void init() {
    tenantService = new LinkedDataTenantService(
      jdbcTemplate,
      context,
      folioSpringLiquibase,
      List.of(testWorker),
      cacheCleaningJob,
      tenantScopedExecutionService
    );
    when(context.getTenantId()).thenReturn(tenantId);
  }

  @Test
  void shouldCallWorker_beforeTenantUpdate() {
    //given
    var attributes = mock(TenantAttributes.class);

    //when
    tenantService.beforeTenantUpdate(attributes);

    //then
    verify(testWorker)
      .beforeTenantUpdate(tenantId, attributes);
  }

  @Test
  void shouldCallWorker_andCleanModuleStateCache_afterTenantUpdate() {
    //given
    var attributes = mock(TenantAttributes.class);

    //when
    tenantService.afterTenantUpdate(attributes);

    //then
    verify(testWorker)
      .afterTenantUpdate(tenantId, attributes);
    verify(cacheCleaningJob).emptyModuleState();
  }

  @Test
  void shouldCallWorker_andCleanModuleStateCache_afterTenantDeletion() {
    //given
    var attributes = mock(TenantAttributes.class);

    //when
    tenantService.afterTenantDeletion(attributes);

    //then
    verify(testWorker)
      .afterTenantDeletion(tenantId);
    verify(cacheCleaningJob).emptyModuleState();
  }
}
