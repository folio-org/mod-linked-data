package org.folio.linked.data.e2e;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.folio.linked.data.e2e.base.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.jdbc.core.JdbcTemplate;

@IntegrationTest
class DatabaseIT {

  private static final String RESOURCES_TABLE_NAME = "resources";

  private static final String LIST_TABLES_QUERY = """
          SELECT table_name
            FROM information_schema.tables
            WHERE table_type = 'BASE TABLE' AND table_schema = ?
    """;
  private static final String INSERT_RESOURCE = "INSERT INTO %s.resources (resource_hash) VALUES (%s)";
  private static final String INSERT_METADATA = "INSERT INTO %s.folio_metadata (resource_hash, srs_id) VALUES (%s, %s)";

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
    assertThat(tables).contains(RESOURCES_TABLE_NAME);
  }

  @Test
  void testUniqueSrsIdPerActiveResourceTrigger() {
    var srsId = 3;
    jdbcTemplate.execute(format(INSERT_RESOURCE, schema, 1));
    jdbcTemplate.execute(format(INSERT_RESOURCE, schema, 2));
    jdbcTemplate.execute(format(INSERT_METADATA, schema, 1, srsId));

    // when
    var thrown = assertThrows(UncategorizedSQLException.class,
      () -> jdbcTemplate.execute(format(INSERT_METADATA, schema, 2, srsId)));

    // then
    assertThat(thrown.getMessage()).contains("There should be only one active resource per unique srs_id");
  }

}
