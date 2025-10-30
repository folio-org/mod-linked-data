package org.folio.linked.data.repo;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public class ResourceEmbeddingRepositoryCustomImpl implements ResourceEmbeddingRepositoryCustom {
  @PersistenceContext
  private EntityManager entityManager;
  @Autowired
  private ResourceRepository resourceRepository;

  @Override
  public Optional<SimilarResourceWithScore> findSimilarAuthority(List<Double> embeddingDbl, double minSimilarity) {
    var embedding = toEmbedding(embeddingDbl);
    String sql = """
      SELECT resource_hash,
             embedding,
             1 - (embedding <=> CAST(:embedding AS vector)) AS similarity
        FROM resource_embeddings
       WHERE 1 - (embedding <=> CAST(:embedding AS vector)) > :minSimilarity
      LIMIT 1
      """;

    Query query = entityManager.createNativeQuery(sql);
    query.setParameter("embedding", embedding);
    query.setParameter("minSimilarity", minSimilarity);

    List<Object[]> results = query.getResultList();
    if (results.isEmpty()) return Optional.empty();

    return results.stream()
      .findFirst()
      .map(row -> {
        var resourceHash = ((Number) row[0]).longValue();
        var resource = resourceRepository.findById(resourceHash).get();
        var similarity = ((Number) row[2]).doubleValue();
        return new SimilarResourceWithScore(
          "similar_resource_exists",
          similarity * 100,
          new SimilarResource(resourceHash, resource.getLabel())
        );
      });
  }

  @Override
  public void upsertEmbedding(Long resourceHash, List<Double> embedding) {
    var embeddingStr = toEmbedding(embedding); // Converts to "[0.1, 0.2, ...]"
    var sql = """
      INSERT INTO resource_embeddings (resource_hash, embedding)
           VALUES (:resourceHash, CAST(:embedding AS vector))
      ON CONFLICT (resource_hash) DO UPDATE SET embedding = EXCLUDED.embedding
      """;
    Query query = entityManager.createNativeQuery(sql);
    query.setParameter("resourceHash", resourceHash);
    query.setParameter("embedding", embeddingStr);
    query.executeUpdate();
  }

  private String toEmbedding(List<Double> embedding) {
    float[] embeddingArray = toFloatArray(embedding);
    StringBuilder sb = new StringBuilder("[");
    for (int i = 0; i < embeddingArray.length; i++) {
      sb.append(embeddingArray[i]);
      if (i < embeddingArray.length - 1) sb.append(",");
    }
    sb.append("]");
    return sb.toString();
  }

  private float[] toFloatArray(List<Double> doubleList) {
    float[] floatArray = new float[doubleList.size()];
    for (int i = 0; i < doubleList.size(); i++) {
      floatArray[i] = doubleList.get(i).floatValue();
    }
    return floatArray;
  }
}
