package org.folio.linked.data.configuration.batch.graph.reader;

import static org.folio.ld.dictionary.ResourceTypeDictionary.HUB;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.folio.linked.data.util.JdbcUtil.COL_RESOURCE_HASH;
import static org.folio.linked.data.util.JdbcUtil.toSqlLiterals;
import static org.springframework.batch.infrastructure.item.database.Order.ASCENDING;

import java.util.Map;
import java.util.Set;
import lombok.experimental.UtilityClass;
import org.springframework.batch.infrastructure.item.database.support.PostgresPagingQueryProvider;

@UtilityClass
public class GraphCleaningQueryProvider {

  private static final Set<Long> EXCLUDED_TYPE_HASHES = Set.of(
    INSTANCE.getHash(),
    HUB.getHash(),
    WORK.getHash()
  );

  private static final String SELECT_CLAUSE = "r.resource_hash";

  private static final String FROM_CLAUSE = "resources r";

  private static final String WHERE_CLAUSE =
    "NOT EXISTS ("
      + "SELECT 1 FROM resource_type_map rtm"
      + " WHERE rtm.resource_hash = r.resource_hash"
      + " AND rtm.type_hash IN (" + toSqlLiterals(EXCLUDED_TYPE_HASHES) + ")"
      + ")"
      + " AND NOT EXISTS ("
      + "SELECT 1 FROM folio_metadata fm"
      + " WHERE fm.resource_hash = r.resource_hash"
      + ")"
      + " AND NOT EXISTS ("
      + "SELECT 1 FROM resource_edges re"
      + " WHERE re.target_hash = r.resource_hash"
      + ")";

  public static PostgresPagingQueryProvider build() {
    var provider = new PostgresPagingQueryProvider();
    provider.setSelectClause(SELECT_CLAUSE);
    provider.setFromClause(FROM_CLAUSE);
    provider.setWhereClause(WHERE_CLAUSE);
    provider.setSortKeys(Map.of(COL_RESOURCE_HASH, ASCENDING));
    return provider;
  }

}



