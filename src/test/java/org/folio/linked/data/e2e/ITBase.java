package org.folio.linked.data.e2e;

import static org.folio.linked.data.test.TestUtil.TENANT_ID;
import static org.folio.linked.data.test.TestUtil.cleanResourceTables;
import static org.folio.linked.data.test.TestUtil.isStandaloneTest;

import org.folio.linked.data.service.resource.hash.HashService;
import org.folio.linked.data.service.tenant.TenantScopedExecutionService;
import org.folio.linked.data.test.resource.ResourceTestService;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

public abstract class ITBase {

  @Autowired
  protected MockMvc mockMvc;
  @Autowired
  protected Environment env;
  @Autowired
  protected HashService hashService;
  @Autowired
  protected JdbcTemplate jdbcTemplate;
  @Autowired
  protected ResourceTestService resourceTestService;
  @Autowired
  protected TenantScopedExecutionService tenantScopedExecutionService;

  @BeforeEach
  public void beforeEach() {
    if (isStandaloneTest(env)) {
      cleanResourceTables(jdbcTemplate);
    } else {
      tenantScopedExecutionService.execute(TENANT_ID, () -> cleanResourceTables(jdbcTemplate));
    }
  }
}
