package org.folio.linked.data.service;

import static org.folio.linked.data.util.Constants.Cache.MODULE_STATE;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class LinkedDataApplicationService implements ApplicationService {

  private static final String TABLE_EXIST_QUERY = """
    SELECT EXISTS (
    SELECT 1
    FROM pg_tables
    WHERE tablename = 'resources'
    AND schemaname = '%TENANT%_mod_linked_data'
    ) AS table_existence""";

  private final JdbcTemplate jdbcTemplate;

  @Override
  @Cacheable(cacheNames = MODULE_STATE)
  public boolean isModuleInstalled(String tenant) {
    return Boolean.TRUE.equals(jdbcTemplate.query(TABLE_EXIST_QUERY.replace("%TENANT%", tenant), rs -> {
      rs.next();
      return rs.getBoolean(1);
    }));
  }
}
