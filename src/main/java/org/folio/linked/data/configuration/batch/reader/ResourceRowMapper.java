package org.folio.linked.data.configuration.batch.reader;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toMap;
import static org.folio.ld.dictionary.ResourceTypeDictionary.MOCKED_RESOURCE;
import static org.folio.linked.data.configuration.batch.reader.IndexableResourceQueryBuilder.COL_DOC;
import static org.folio.linked.data.configuration.batch.reader.IndexableResourceQueryBuilder.COL_INCOMING_EDGES;
import static org.folio.linked.data.configuration.batch.reader.IndexableResourceQueryBuilder.COL_LABEL;
import static org.folio.linked.data.configuration.batch.reader.IndexableResourceQueryBuilder.COL_OUTGOING_EDGES;
import static org.folio.linked.data.configuration.batch.reader.IndexableResourceQueryBuilder.COL_RESOURCE_HASH;
import static org.folio.linked.data.configuration.batch.reader.IndexableResourceQueryBuilder.COL_TYPE_HASHES;
import static org.folio.linked.data.configuration.batch.reader.IndexableResourceQueryBuilder.EDGE_PREDICATE_HASH;
import static org.folio.linked.data.configuration.batch.reader.IndexableResourceQueryBuilder.EDGE_SOURCE_DOC;
import static org.folio.linked.data.configuration.batch.reader.IndexableResourceQueryBuilder.EDGE_SOURCE_HASH;
import static org.folio.linked.data.configuration.batch.reader.IndexableResourceQueryBuilder.EDGE_SOURCE_LABEL;
import static org.folio.linked.data.configuration.batch.reader.IndexableResourceQueryBuilder.EDGE_SOURCE_OUTGOING;
import static org.folio.linked.data.configuration.batch.reader.IndexableResourceQueryBuilder.EDGE_SOURCE_TYPES;
import static org.folio.linked.data.configuration.batch.reader.IndexableResourceQueryBuilder.EDGE_TARGET_DOC;
import static org.folio.linked.data.configuration.batch.reader.IndexableResourceQueryBuilder.EDGE_TARGET_HASH;
import static org.folio.linked.data.configuration.batch.reader.IndexableResourceQueryBuilder.EDGE_TARGET_LABEL;
import static org.folio.linked.data.configuration.batch.reader.IndexableResourceQueryBuilder.EDGE_TARGET_TYPES;
import static org.folio.linked.data.util.JsonUtils.JSON_MAPPER;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.model.entity.PredicateEntity;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.model.entity.ResourceTypeEntity;
import org.folio.linked.data.util.IndexableEdges;
import org.springframework.jdbc.core.RowMapper;
import tools.jackson.databind.JsonNode;

@UtilityClass
public class ResourceRowMapper {

  private static final Map<Long, ResourceTypeEntity> TYPE_ENTITY_BY_HASH = stream(ResourceTypeDictionary.values())
    .filter(t -> t != MOCKED_RESOURCE)
    .collect(toMap(ResourceTypeDictionary::getHash,
      t -> new ResourceTypeEntity(t.getHash(), t.getUri(), null)
    ));

  private static final Map<Long, PredicateEntity> PREDICATE_ENTITY_BY_HASH = Stream.concat(
      IndexableEdges.OUTGOING.stream(), IndexableEdges.INCOMING.stream())
    .collect(toMap(PredicateDictionary::getHash,
      p -> new PredicateEntity(p.getHash(), p.getUri())
    ));

  public static RowMapper<Resource> instance() {
    return ResourceRowMapper::mapRow;
  }

  @SneakyThrows
  private static Resource mapRow(ResultSet rs, int rowNum) {
    var resource = new Resource();
    resource.setIdAndRefreshEdges(rs.getLong(COL_RESOURCE_HASH));
    resource.setLabel(rs.getString(COL_LABEL));
    var docStr = rs.getString(COL_DOC);
    if (docStr != null) {
      resource.setDoc(JSON_MAPPER.readTree(docStr));
    }
    resource.setTypes(mapTypes(rs.getArray(COL_TYPE_HASHES)));
    mapOutgoingEdges(rs.getString(COL_OUTGOING_EDGES), resource);
    mapIncomingEdges(rs.getString(COL_INCOMING_EDGES), resource);
    return resource;
  }

  private static Set<ResourceTypeEntity> mapTypes(Array typeHashesArray) throws SQLException {
    var types = new LinkedHashSet<ResourceTypeEntity>();
    if (typeHashesArray == null) {
      return types;
    }
    for (Long hash : (Long[]) typeHashesArray.getArray()) {
      var entity = TYPE_ENTITY_BY_HASH.get(hash);
      if (entity != null) {
        types.add(entity);
      }
    }
    return types;
  }

  private static void mapOutgoingEdges(String json, Resource source) {
    if (json == null) {
      return;
    }
    for (JsonNode node : JSON_MAPPER.readTree(json)) {
      var predicate = PREDICATE_ENTITY_BY_HASH.get(node.get(EDGE_PREDICATE_HASH).longValue());
      if (predicate == null) {
        continue;
      }
      var target = buildResource(
        node.get(EDGE_TARGET_HASH).longValue(),
        node.get(EDGE_TARGET_LABEL).asString(),
        node.get(EDGE_TARGET_DOC),
        node.get(EDGE_TARGET_TYPES)
      );
      source.addOutgoingEdge(new ResourceEdge(source, target, predicate));
    }
  }

  private static void mapIncomingEdges(String json, Resource target) {
    if (json == null) {
      return;
    }
    for (JsonNode node : JSON_MAPPER.readTree(json)) {
      var predicate = PREDICATE_ENTITY_BY_HASH.get(node.get(EDGE_PREDICATE_HASH).longValue());
      if (predicate == null) {
        continue;
      }
      var source = buildResource(
        node.get(EDGE_SOURCE_HASH).longValue(),
        node.get(EDGE_SOURCE_LABEL).asString(),
        node.get(EDGE_SOURCE_DOC),
        node.get(EDGE_SOURCE_TYPES)
      );
      var sourceOutgoingNode = node.get(EDGE_SOURCE_OUTGOING);
      if (sourceOutgoingNode != null && !sourceOutgoingNode.isNull()) {
        mapOutgoingEdges(sourceOutgoingNode.toString(), source);
      }
      target.addIncomingEdge(new ResourceEdge(source, target, predicate));
    }
  }

  private static Resource buildResource(long id, String label, JsonNode doc, JsonNode typeHashesNode) {
    var resource = new Resource();
    resource.setIdAndRefreshEdges(id);
    resource.setLabel(label);
    if (doc != null && !doc.isNull()) {
      resource.setDoc(doc);
    }
    var types = new LinkedHashSet<ResourceTypeEntity>();
    if (typeHashesNode != null && !typeHashesNode.isNull()) {
      for (JsonNode hashNode : typeHashesNode) {
        var entity = TYPE_ENTITY_BY_HASH.get(hashNode.longValue());
        if (entity != null) {
          types.add(entity);
        }
      }
    }
    resource.setTypes(types);
    return resource;
  }
}
