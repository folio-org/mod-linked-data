package org.folio.linked.data.e2e;

import static org.assertj.core.api.Assertions.assertThat;

import org.folio.linked.data.e2e.base.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;

@IntegrationTest
class DatabaseIT {

  private static final String GRAPHSET_TABLE_NAME = "graphset";

  private static final String LIST_TABLES_QUERY = """
          SELECT table_name
            FROM information_schema.tables
            WHERE table_type = 'BASE TABLE' AND table_schema = ?
    """;

  @Value("${spring.application.name}")
  private String appName;
  @Value("${spring.jpa.properties.hibernate.default_schema}")
  private String schema;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Test
  void testTablesCreated() {
    // when
    var tables = jdbcTemplate.queryForList(LIST_TABLES_QUERY, String.class, schema);

    // then
    assertThat(tables).contains(GRAPHSET_TABLE_NAME);
  }

}
