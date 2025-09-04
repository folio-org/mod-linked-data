package org.folio.linked.data.e2e.database;

import static org.assertj.core.api.Assertions.assertThat;

import org.folio.spring.tools.kafka.KafkaAdminService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

abstract class DatabaseITBase {

  private static final String RESOURCES_TABLE_NAME = "resources";

  private static final String LIST_TABLES_QUERY = """
          SELECT table_name
            FROM information_schema.tables
            WHERE table_type = 'BASE TABLE' AND table_schema = ?
    """;

  @Value("${spring.jpa.properties.hibernate.default_schema}")
  private String schema;

  @Autowired
  private JdbcTemplate jdbcTemplate;
  @MockitoSpyBean
  private KafkaAdminService kafkaAdminService;

  @Test
  void testTablesCreated() {
    // when
    var tables = jdbcTemplate.queryForList(LIST_TABLES_QUERY, String.class, schema);

    // then
    assertThat(tables).contains(RESOURCES_TABLE_NAME);
  }

}
