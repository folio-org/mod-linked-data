package org.folio.linked.data.service.impl.tenant;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;
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
class LinkedTenantServiceTest {

  @Mock
  JdbcTemplate jdbcTemplate;
  @Mock
  FolioExecutionContext context;
  @Mock
  FolioSpringLiquibase folioSpringLiquibase;
  @Mock
  TenantServiceWorker testWorker;

  LinkedTenantService tenantService;

  @BeforeEach
  void init() {
    tenantService = new LinkedTenantService(
      jdbcTemplate,
      context,
      folioSpringLiquibase,
      List.of(testWorker)
    );
  }

  @Test
  void shouldCallWorker_beforeTenantUpdate() {
    //given
    var attributes = mock(TenantAttributes.class);

    //when
    tenantService.beforeTenantUpdate(attributes);

    //then
    verify(testWorker)
      .beforeTenantUpdate(attributes);
  }

  @Test
  void shouldCallWorker_afterTenantUpdate() {
    //given
    var attributes = mock(TenantAttributes.class);

    //when
    tenantService.afterTenantUpdate(attributes);

    //then
    verify(testWorker)
      .afterTenantUpdate(attributes);
  }
}
