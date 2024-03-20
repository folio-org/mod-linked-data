package org.folio.linked.data.e2e.dictionary;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.linked.data.model.entity.PredicateEntity;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.test.jdbc.JdbcTestUtils;

@IntegrationTest
class PredicateDictionaryIT {

  private static final String TABLE_NAME = "predicate_lookup";

  @Value("${spring.jpa.properties.hibernate.default_schema}")
  private String schema;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Test
  void countPredicates_shouldSameAsLibPredicates() {
    //given
    var table = String.format("%s.%s", schema, TABLE_NAME);

    // when
    var countDb = JdbcTestUtils.countRowsInTable(jdbcTemplate, table);
    var countLib = PredicateDictionary.values().length - 1; // exclude PredicateDictionary.NULL

    //then
    assertThat(countDb)
      .isEqualTo(countLib);
  }

  @ParameterizedTest
  @EnumSource(value = PredicateDictionary.class, mode = EnumSource.Mode.EXCLUDE, names = {"NULL"})
  void valueInLib_shouldBeTheSameAsInDb(PredicateDictionary predicate) {
    //given
    var uri = predicate.getUri();
    var sql = String.format("SELECT predicate_hash, predicate FROM %s.%s WHERE predicate = ?", schema, TABLE_NAME);

    // when
    var dbPredicate = jdbcTemplate.queryForObject(
      sql, new Object[]{uri}, new int[]{java.sql.Types.VARCHAR}, new PredicateEntityMapper());

    //then
    assertThat(dbPredicate)
      .isNotNull()
      .extracting(PredicateEntity::getHash)
      .isEqualTo(predicate.getHash());
  }

  @TestFactory
  List<DynamicTest> valueInDb_shouldBeInLib() {
    //given
    var sql = String.format("SELECT predicate_hash, predicate FROM %s.%s", schema, TABLE_NAME);
    var testData = jdbcTemplate.query(sql, new PredicateEntityMapper());

    return testData.stream()
      .map(dbPredicate -> DynamicTest.dynamicTest("Test for predicate: " + dbPredicate.toString(),
        () -> {
          // when
          var predicate = PredicateDictionary.fromUri(dbPredicate.getUri());

          //then
          assertThat(predicate)
            .isNotEmpty()
            .map(PredicateDictionary::getHash)
            .contains(dbPredicate.getHash());
        }))
      .collect(Collectors.toList());
  }

  private static final class PredicateEntityMapper implements RowMapper<PredicateEntity> {

    @Override
    public PredicateEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
      var entity = new PredicateEntity();
      entity.setHash(rs.getLong("predicate_hash"));
      entity.setUri(rs.getString("predicate"));
      return entity;
    }
  }
}
