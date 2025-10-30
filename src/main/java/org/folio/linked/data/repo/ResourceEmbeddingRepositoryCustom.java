package org.folio.linked.data.repo;

import java.util.List;
import java.util.Optional;

public interface ResourceEmbeddingRepositoryCustom {
  Optional<SimilarResource> findSimilarAuthority(List<Double> embedding, double minSimilarity);

  void upsertEmbedding(Long resourceHash, List<Double> embedding);

  record SimilarResource(String reason, Long existingResourceId, String label, double similarityScore) {}
}
