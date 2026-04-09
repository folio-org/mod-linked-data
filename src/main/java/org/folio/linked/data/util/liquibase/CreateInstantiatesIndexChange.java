package org.folio.linked.data.util.liquibase;

import static org.folio.ld.dictionary.PredicateDictionary.INSTANTIATES;

import java.sql.Connection;
import java.sql.SQLException;
import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;

public class CreateInstantiatesIndexChange implements CustomTaskChange {

  @Override
  public void execute(Database database) throws CustomChangeException {
    try {
      var jdbcConnection = (JdbcConnection) database.getConnection();
      var connection = jdbcConnection.getUnderlyingConnection();
      var schema = connection.getSchema();
      createIndex(connection, schema);
    } catch (Exception e) {
      throw new CustomChangeException("Failed to create resource_edges_single_instantiates_uidx", e);
    }
  }

  @SuppressWarnings("java:S2077")
  private void createIndex(Connection connection, String schema) throws SQLException {
    var sql = """
      CREATE UNIQUE INDEX IF NOT EXISTS resource_edges_single_instantiates_uidx
        ON %s.resource_edges (source_hash, predicate_hash)
        WHERE predicate_hash = %d
      """.formatted(schema, INSTANTIATES.getHash());
    try (var stmt = connection.createStatement()) {
      stmt.execute(sql);
    }
  }

  @Override
  public String getConfirmationMessage() {
    return "Index resource_edges_single_instantiates_uidx created with predicate_hash = "
      + INSTANTIATES.getHash();
  }

  @Override
  public void setUp() throws SetupException {
    // nothing to set up
  }

  @Override
  public void setFileOpener(ResourceAccessor resourceAccessor) {
    // no need to set
  }

  @Override
  public ValidationErrors validate(Database database) {
    return new ValidationErrors();
  }
}

