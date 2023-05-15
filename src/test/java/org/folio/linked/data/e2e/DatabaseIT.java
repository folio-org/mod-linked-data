package org.folio.linked.data.e2e;

import static org.assertj.core.api.Assertions.assertThat;

import org.folio.linked.data.TestUtil;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

@IntegrationTest
class DatabaseIT {

  private static final String GRAPHSET_TABLE_NAME = "graphset";

  private static final String LIST_TABLES_QUERY = """
          SELECT table_name
            FROM information_schema.tables
            WHERE table_type = 'BASE TABLE' AND table_schema = ?
    """;

  @Autowired
  private ApplicationContext context;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Test
  void testTablesCreated() {
    // given
    var appName = context.getEnvironment().getProperty("spring.application.name");
    assertThat(appName).isNotNull();
    var schema = TestUtil.TENANT_ID + "_" + appName.replace('-', '_');

    // when
    var tables = jdbcTemplate.queryForList(LIST_TABLES_QUERY, String.class, schema);

    // then
    assertThat(tables).contains(GRAPHSET_TABLE_NAME);
  }

}
