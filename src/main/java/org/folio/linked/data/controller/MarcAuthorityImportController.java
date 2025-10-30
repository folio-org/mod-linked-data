package org.folio.linked.data.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.folio.linked.data.embedding.ResourceEmbeddingService;
import org.folio.linked.data.exception.RequestProcessingExceptionBuilder;
import org.folio.linked.data.mapper.ResourceModelMapper;
import org.folio.linked.data.repo.ResourceEmbeddingRepositoryCustom;
import org.folio.linked.data.service.resource.graph.ResourceGraphService;
import org.folio.marc4ld.service.marc2ld.authority.MarcAuthority2ldMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/graph/import")
@RequiredArgsConstructor
public class MarcAuthorityImportController {
  private static final Double SIMILARITY_THRESHOLD = 0.85;

  private final MarcAuthority2ldMapper marcAuthority2ldMapper;
  private final ResourceEmbeddingService embeddingService;
  private final ResourceEmbeddingRepositoryCustom resourceEmbeddingRepository;
  private final ResourceModelMapper resourceModelMapper;
  private final ResourceGraphService resourceGraphService;

  @PostMapping("/from-marc-authority")
  @SneakyThrows
  public ResponseEntity<Object> create(@RequestBody String marc) {
    var resource = marcAuthority2ldMapper.fromMarcJson(marc);
    var entity = resourceModelMapper.toEntity(resource.stream().findFirst().get());
    var embedding = embeddingService.embedResource(entity);
    if (!embedding.isEmpty()) {
      var similar = resourceEmbeddingRepository.findSimilarAuthority(embedding, SIMILARITY_THRESHOLD);
      if (similar.isPresent()) {
        return ResponseEntity.badRequest().body(similar.get());
      }
    }
    resourceGraphService.saveMergingGraph(entity);
    return ResponseEntity.noContent().build();
  }
}
