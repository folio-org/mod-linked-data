package org.folio.linked.data.e2e.dictionary;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.model.entity.ResourceTypeEntity;
import org.folio.spring.tools.kafka.KafkaAdminService;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.jdbc.JdbcTestUtils;

abstract class ResourceTypeDictionaryITBase {

  private static final String TABLE_NAME = "type_lookup";

  @Value("${spring.jpa.properties.hibernate.default_schema}")
  private String schema;
  @Autowired
  private JdbcTemplate jdbcTemplate;
  @MockitoBean
  private KafkaAdminService kafkaAdminService;

  @Test
  void countTypes_shouldSameAsLibTypes() {
    //given
    var table = String.format("%s.%s", schema, TABLE_NAME);

    // when
    var countDb = JdbcTestUtils.countRowsInTable(jdbcTemplate, table);
    var countLib = ResourceTypeDictionary.values().length;

    //then
    assertThat(countDb)
      .isEqualTo(countLib);
  }

  @ParameterizedTest
  @EnumSource(value = ResourceTypeDictionary.class)
  void valueInLib_shouldBeTheSameAsInDb(ResourceTypeDictionary type) {
    //given
    var uri = type.getUri();
    var sql = String.format("SELECT type_hash, type_uri FROM %s.%s WHERE type_uri = ?", schema, TABLE_NAME);

    // when
    var dbType = jdbcTemplate.queryForObject(
      sql, new Object[]{uri}, new int[]{java.sql.Types.VARCHAR}, new ResourceTypeEntityMapper());

    //then
    assertThat(dbType)
      .isNotNull()
      .extracting(ResourceTypeEntity::getHash)
      .isEqualTo(type.getHash());
  }

  @TestFactory
  List<DynamicTest> valueInDb_shouldBeInLib() {
    //given
    var sql = String.format("SELECT type_hash, type_uri FROM %s.%s", schema, TABLE_NAME);
    var testData = jdbcTemplate.query(sql, new ResourceTypeEntityMapper());

    return testData.stream()
      .map(dbType -> DynamicTest.dynamicTest("Test for type: " + dbType.toString(),
        () -> {
          // when
          var type = ResourceTypeDictionary.fromUri(dbType.getUri());

          //then
          assertThat(type)
            .isNotEmpty()
            .map(ResourceTypeDictionary::getHash)
            .contains(dbType.getHash());
        }))
      .toList();
  }

  private static final class ResourceTypeEntityMapper implements RowMapper<ResourceTypeEntity> {

    @Override
    public ResourceTypeEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
      var entity = new ResourceTypeEntity();
      entity.setHash(rs.getLong("type_hash"));
      entity.setUri(rs.getString("type_uri"));
      return entity;
    }
  }
}
