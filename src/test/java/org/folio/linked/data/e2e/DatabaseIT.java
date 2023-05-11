package org.folio.linked.data.e2e;

import java.util.List;
import org.assertj.core.api.Assertions;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.spring.test.extension.impl.OkapiConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
class DatabaseIT {

  private static MockMvc mockMvc;
  private static OkapiConfiguration okapi;

  private static final String GRAPHSET_SCHEMA_NAME = "mod-linked-data";
  private static final String GRAPHSET_TABLE_NAME = "graphset";
  private final String LIST_TABLES_QUERY = """
            SELECT table_name
              FROM information_schema.tables
              WHERE table_type = 'BASE TABLE' AND table_schema = ?
      """;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Test
  void testTablesCreated() {
    List<String> tables = jdbcTemplate.queryForList(LIST_TABLES_QUERY, String.class, GRAPHSET_SCHEMA_NAME);
    System.out.println(tables);
    Assertions.assertThat(tables).contains(GRAPHSET_TABLE_NAME);
  }

}
