package org.folio.linked.data.repo;

import java.util.List;
import java.util.Optional;

public interface ResourceEmbeddingRepositoryCustom {
  Optional<SimilarResourceWithScore> findSimilarAuthority(List<Double> embedding, double minSimilarity);

  void upsertEmbedding(Long resourceHash, List<Double> embedding);

  record SimilarResourceWithScore(String reason, double similarityScore, SimilarResource existingResource) {}
  record SimilarResource(Long id, String label){}
}
