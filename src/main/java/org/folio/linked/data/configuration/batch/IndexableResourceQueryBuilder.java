package org.folio.linked.data.configuration.batch;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.folio.ld.dictionary.ResourceTypeDictionary.valueOf;
import static org.folio.linked.data.configuration.batch.BatchConfig.SUPPORTED_TYPES;
import static org.springframework.batch.infrastructure.item.database.Order.ASCENDING;

import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.experimental.UtilityClass;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.util.IndexableEdges;
import org.springframework.batch.infrastructure.item.database.support.PostgresPagingQueryProvider;

@UtilityClass
public class IndexableResourceQueryBuilder {

  static final Set<Long> INDEXABLE_OUTGOING_PREDICATES = IndexableEdges.OUTGOING.stream()
    .map(PredicateDictionary::getHash)
    .collect(toSet());

  static final Set<Long> INDEXABLE_INCOMING_PREDICATES = IndexableEdges.INCOMING.stream()
    .map(PredicateDictionary::getHash)
    .collect(toSet());


  static final String COL_RESOURCE_HASH = "resource_hash";
  static final String COL_LABEL = "label";
  static final String COL_DOC = "doc";
  static final String COL_TYPE_HASHES = "type_hashes";
  static final String COL_OUTGOING_EDGES = "outgoing_edges";
  static final String COL_INCOMING_EDGES = "incoming_edges";
  static final String EDGE_PREDICATE_HASH = "predicateHash";
  static final String EDGE_TARGET_HASH = "targetHash";
  static final String EDGE_TARGET_LABEL = "targetLabel";
  static final String EDGE_TARGET_DOC = "targetDoc";
  static final String EDGE_TARGET_TYPES = "targetTypes";
  static final String EDGE_SOURCE_HASH = "sourceHash";
  static final String EDGE_SOURCE_LABEL = "sourceLabel";
  static final String EDGE_SOURCE_DOC = "sourceDoc";
  static final String EDGE_SOURCE_TYPES = "sourceTypes";
  static final String EDGE_SOURCE_OUTGOING = "sourceOutgoing";

  private static final String TYPE_HASHES_PARAM = "typeHashes";

  private static final String OUTGOING_EDGE_AGG =
    "CASE WHEN EXISTS ("
      + "  SELECT 1 FROM resource_type_map rtm2"
      + "  WHERE rtm2.resource_hash = r.resource_hash"
      + "    AND rtm2.type_hash = " + WORK.getHash()
      + ") THEN ("
      + "  SELECT json_agg(json_build_object("
      + "    '" + EDGE_PREDICATE_HASH + "', e.predicate_hash,"
      + "    '" + EDGE_TARGET_HASH + "',    t.resource_hash,"
      + "    '" + EDGE_TARGET_LABEL + "',   t.label,"
      + "    '" + EDGE_TARGET_DOC + "',     t.doc,"
      + "    '" + EDGE_TARGET_TYPES + "',   (SELECT array_agg(tm.type_hash)"
      + "                      FROM resource_type_map tm"
      + "                      WHERE tm.resource_hash = t.resource_hash)"
      + "  ))"
      + "  FROM resource_edges e"
      + "  JOIN resources t ON t.resource_hash = e.target_hash"
      + "  WHERE e.source_hash = r.resource_hash"
      + "    AND e.predicate_hash IN (" + toSqlLiterals(INDEXABLE_OUTGOING_PREDICATES) + ")"
      + ") END AS " + COL_OUTGOING_EDGES;

  private static final String INCOMING_EDGE_AGG =
    "CASE WHEN EXISTS ("
      + "  SELECT 1 FROM resource_type_map rtm2"
      + "  WHERE rtm2.resource_hash = r.resource_hash"
      + "    AND rtm2.type_hash = " + WORK.getHash()
      + ") THEN ("
      + "  SELECT json_agg(json_build_object("
      + "    '" + EDGE_PREDICATE_HASH + "', e.predicate_hash,"
      + "    '" + EDGE_SOURCE_HASH + "',    s.resource_hash,"
      + "    '" + EDGE_SOURCE_LABEL + "',   s.label,"
      + "    '" + EDGE_SOURCE_DOC + "',     s.doc,"
      + "    '" + EDGE_SOURCE_TYPES + "',   (SELECT array_agg(tm.type_hash)"
      + "                      FROM resource_type_map tm"
      + "                      WHERE tm.resource_hash = s.resource_hash),"
      + "    '" + EDGE_SOURCE_OUTGOING + "', (SELECT json_agg(json_build_object("
      + "        '" + EDGE_PREDICATE_HASH + "', ie.predicate_hash,"
      + "        '" + EDGE_TARGET_HASH + "',    it.resource_hash,"
      + "        '" + EDGE_TARGET_LABEL + "',   it.label,"
      + "        '" + EDGE_TARGET_DOC + "',     it.doc,"
      + "        '" + EDGE_TARGET_TYPES + "',   (SELECT array_agg(itm.type_hash)"
      + "                          FROM resource_type_map itm"
      + "                          WHERE itm.resource_hash = it.resource_hash)"
      + "      ))"
      + "      FROM resource_edges ie"
      + "      JOIN resources it ON it.resource_hash = ie.target_hash"
      + "      WHERE ie.source_hash = s.resource_hash"
      + "        AND ie.predicate_hash IN (" + toSqlLiterals(INDEXABLE_OUTGOING_PREDICATES) + "))"
      + "  ))"
      + "  FROM resource_edges e"
      + "  JOIN resources s ON s.resource_hash = e.source_hash"
      + "  WHERE e.target_hash = r.resource_hash"
      + "    AND e.predicate_hash IN (" + toSqlLiterals(INDEXABLE_INCOMING_PREDICATES) + ")"
      + ") END AS " + COL_INCOMING_EDGES;

  private static final String SELECT_CLAUSE =
    "r." + COL_RESOURCE_HASH + ", r." + COL_LABEL + ", r." + COL_DOC + ","
      + " array_agg(DISTINCT rtm.type_hash) AS " + COL_TYPE_HASHES + ","
      + " " + OUTGOING_EDGE_AGG + ","
      + " " + INCOMING_EDGE_AGG;

  private static final String FROM_CLAUSE =
    "resources r JOIN resource_type_map rtm ON r.resource_hash = rtm.resource_hash";
  private static final String WHERE_FULL =
    "rtm.type_hash IN (:" + TYPE_HASHES_PARAM + ")";
  private static final String WHERE_NOT_INDEXED =
    "r.index_date IS NULL AND rtm.type_hash IN (:" + TYPE_HASHES_PARAM + ")";
  private static final String GROUP_BY_CLAUSE = "r.resource_hash";
  private static final String SORT_KEY = "resource_hash";

  public static PostgresPagingQueryProvider buildQueryProvider(boolean isFullReindex) {
    var provider = new PostgresPagingQueryProvider();
    provider.setSelectClause(SELECT_CLAUSE);
    provider.setFromClause(FROM_CLAUSE);
    provider.setWhereClause(isFullReindex ? WHERE_FULL : WHERE_NOT_INDEXED);
    provider.setGroupClause(GROUP_BY_CLAUSE);
    provider.setSortKeys(Map.of(SORT_KEY, ASCENDING));
    return provider;
  }

  public static Map<String, Object> buildParameterValues(String resourceType) {
    return Map.of(TYPE_HASHES_PARAM, resolveTypeHashes(resourceType));
  }

  private static List<Long> resolveTypeHashes(String resourceType) {
    if (isBlank(resourceType)) {
      return SUPPORTED_TYPES.stream()
        .map(ResourceTypeDictionary::getHash)
        .toList();
    }
    return List.of(valueOf(resourceType.toUpperCase()).getHash());
  }

  private static String toSqlLiterals(Set<Long> hashes) {
    return hashes.stream()
      .map(String::valueOf)
      .collect(joining(", "));
  }
}
