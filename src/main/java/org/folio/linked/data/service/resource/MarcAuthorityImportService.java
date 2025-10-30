package org.folio.linked.data.service.resource;
import static org.folio.linked.data.repo.ResourceEmbeddingRepositoryCustom.SimilarResourceWithScore;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.folio.linked.data.embedding.ResourceEmbeddingService;
import org.folio.linked.data.mapper.ResourceModelMapper;

import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.repo.ResourceEmbeddingRepositoryCustom;
import org.folio.linked.data.service.resource.graph.ResourceGraphService;
import org.folio.marc4ld.service.marc2ld.authority.MarcAuthority2ldMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MarcAuthorityImportService {
  private static final Double SIMILARITY_THRESHOLD = 0.80;

  private final MarcAuthority2ldMapper marcAuthority2ldMapper;
  private final ResourceEmbeddingService embeddingService;
  private final ResourceEmbeddingRepositoryCustom resourceEmbeddingRepository;
  private final ResourceModelMapper resourceModelMapper;
  private final ResourceGraphService resourceGraphService;

  @SneakyThrows
  public ImportResult importMarcAuthority(String marc, boolean force) {
    var resource = marcAuthority2ldMapper.fromMarcJson(marc).stream().findFirst();

    if (resource.isEmpty()) {
      throw new IllegalArgumentException("Marc Authority could not be parsed");
    }

    var entity = resourceModelMapper.toEntity(resource.get());
    if (!force) {
      var embedding = embeddingService.embedResource(entity);

      if (!embedding.isEmpty()) {
        var similar = resourceEmbeddingRepository.findSimilarAuthority(embedding, SIMILARITY_THRESHOLD);
        if (similar.isPresent()) {
          return ImportResult.failure(similar.get());
        }
      }
    }

    var result = resourceGraphService.saveMergingGraph(entity);
    return ImportResult.success(result.rootResource());
  }

  public record ImportResult(Resource success, SimilarResourceWithScore failure) {
    public static ImportResult success(Resource resource) {
      return new ImportResult(resource, null);
    }

    public static ImportResult failure(SimilarResourceWithScore failure) {
      return new ImportResult(null, failure);
    }

    public boolean isSuccess() {
      return success != null;
    }
  }
}

